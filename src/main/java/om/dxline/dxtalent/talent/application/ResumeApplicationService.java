package om.dxline.dxtalent.talent.application;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import om.dxline.dxtalent.identity.domain.model.UserId;
import om.dxline.dxtalent.talent.application.command.SearchResumesCommand;
import om.dxline.dxtalent.talent.application.command.UploadResumeCommand;
import om.dxline.dxtalent.talent.application.result.ResumeResult;
import om.dxline.dxtalent.talent.domain.model.*;
import om.dxline.dxtalent.talent.domain.port.FileStoragePort;
import om.dxline.dxtalent.talent.domain.port.ResumeParsingPort;
import om.dxline.dxtalent.talent.domain.repository.ResumeRepository;
import om.dxline.dxtalent.talent.domain.service.ResumeDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Resume Application Service
 *
 * 이력서 관련 유스케이스를 오케스트레이션하는 애플리케이션 서비스입니다.
 *
 * 책임:
 * - 유스케이스 조정 (도메인 서비스, Repository, Port 호출)
 * - 트랜잭션 경계 관리
 * - 도메인 모델 ↔ DTO 변환
 * - 권한 검증
 * - 예외 처리 및 로깅
 *
 * 설계 원칙:
 * - 비즈니스 로직은 도메인 레이어에 위임
 * - Application Service는 오케스트레이션만 담당
 * - 외부 인프라는 Port를 통해 접근
 *
 * 사용 예시:
 * <pre>
 * ResumeResult result = resumeApplicationService.uploadResume(command);
 * </pre>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeApplicationService {

    private final ResumeRepository resumeRepository;
    private final ResumeDomainService resumeDomainService;
    private final FileStoragePort fileStoragePort;
    private final ResumeParsingPort resumeParsingPort;

    /**
     * 이력서 업로드
     *
     * 유스케이스:
     * 1. 파일을 스토리지(S3)에 업로드
     * 2. Resume 애그리게이트 생성
     * 3. Repository에 저장
     * 4. 도메인 이벤트 발행 (파싱 트리거)
     *
     * @param command 업로드 커맨드
     * @return 업로드된 이력서 정보
     * @throws IllegalArgumentException 유효하지 않은 입력
     * @throws FileStoragePort.FileStorageException 파일 업로드 실패
     */
    @Transactional
    public ResumeResult uploadResume(UploadResumeCommand command) {
        log.info("Starting resume upload for user: {}, fileName: {}",
            command.getUserId(), command.getOriginalFileName());

        try {
            // 1. 파일명 및 타입 검증
            FileName fileName = new FileName(command.getOriginalFileName());
            FileSize fileSize = new FileSize(command.getFileSize());
            FileType fileType = FileType.fromFileName(command.getOriginalFileName());

            if (!fileType.isSupported()) {
                throw new IllegalArgumentException(
                    "지원하지 않는 파일 형식입니다. 허용: " +
                    String.join(", ", FileType.getSupportedExtensions())
                );
            }

            // 2. 파일 스토리지에 업로드
            FileStoragePort.FileUploadResult uploadResult = fileStoragePort.uploadFile(
                command.getFileInputStream(),
                fileName,
                fileSize,
                fileType,
                command.getUserId()
            );

            log.debug("File uploaded to storage: {}", uploadResult.getStorageKey());

            // 3. Resume 애그리게이트 생성 (업로드 완료)
            UserId userId = UserId.of(command.getUserId());
            Resume resume = Resume.upload(
                userId,
                fileName,
                fileSize,
                fileType,
                uploadResult.getStorageKey()
            );

            // 4. 중복 확인 (선택적)
            List<Resume> existingResumes = resumeRepository.findByUserId(userId);
            boolean isDuplicate = resumeDomainService.isDuplicateResume(resume, existingResumes);
            if (isDuplicate) {
                log.warn("Duplicate resume detected for user: {}", userId.getValue());
                // 경고만 하고 계속 진행 (비즈니스 정책에 따라 변경 가능)
            }

            // 5. Repository에 저장
            Resume savedResume = resumeRepository.save(resume);

            log.info("Resume uploaded successfully: resumeId={}, userId={}",
                savedResume.getId().getValue(), userId.getValue());

            // 6. 결과 반환 (도메인 이벤트는 Repository에서 자동 발행)
            return ResumeResult.from(savedResume);

        } catch (FileStoragePort.FileStorageException e) {
            log.error("Failed to upload file to storage", e);
            throw new RuntimeException("파일 업로드에 실패했습니다: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error during resume upload", e);
            throw new RuntimeException("이력서 업로드 중 오류가 발생했습니다", e);
        }
    }

    /**
     * 이력서 조회 (단건)
     *
     * @param resumeId 이력서 ID
     * @param userId 사용자 ID (권한 검증용)
     * @return 이력서 정보
     * @throws IllegalArgumentException 이력서를 찾을 수 없거나 권한이 없는 경우
     */
    @Transactional(readOnly = true)
    public ResumeResult getResume(Long resumeId, Long userId) {
        log.debug("Getting resume: resumeId={}, userId={}", resumeId, userId);

        ResumeId id = ResumeId.of(resumeId);
        UserId uid = UserId.of(userId);

        Resume resume = resumeRepository.findByIdAndUserId(id, uid)
            .orElseThrow(() -> new IllegalArgumentException("이력서를 찾을 수 없습니다"));

        return ResumeResult.from(resume);
    }

    /**
     * 사용자의 이력서 목록 조회
     *
     * @param userId 사용자 ID
     * @return 이력서 목록
     */
    @Transactional(readOnly = true)
    public List<ResumeResult> getUserResumes(Long userId) {
        log.debug("Getting resumes for user: {}", userId);

        UserId uid = UserId.of(userId);
        List<Resume> resumes = resumeRepository.findByUserId(uid);

        return resumes.stream()
            .map(ResumeResult::from)
            .collect(Collectors.toList());
    }

    /**
     * 이력서 검색
     *
     * 유스케이스:
     * 1. 모든 이력서 조회 (또는 필터링된 이력서)
     * 2. 도메인 서비스를 통한 복합 검색
     * 3. 매칭 점수 계산 및 정렬
     * 4. 페이징 처리
     *
     * @param command 검색 커맨드
     * @return 매칭 점수를 포함한 이력서 목록
     */
    @Transactional(readOnly = true)
    public List<ResumeResult.ResumeResultWithScore> searchResumes(SearchResumesCommand command) {
        log.info("Searching resumes with criteria: {}", command);

        // 1. 전체 이력서 조회 (파싱 완료된 것만)
        List<Resume> allResumes = resumeRepository.findAll();
        List<Resume> parsedResumes = allResumes.stream()
            .filter(Resume::isParsed)
            .collect(Collectors.toList());

        log.debug("Found {} parsed resumes", parsedResumes.size());

        // 2. 도메인 서비스를 통한 복합 검색
        ResumeDomainService.SearchCriteria criteria = new ResumeDomainService.SearchCriteria();
        criteria.setRequiredSkills(command.getRequiredSkills());
        criteria.setPreferredSkills(command.getPreferredSkills());
        criteria.setMinimumYearsOfExperience(command.getMinimumYearsOfExperience());
        criteria.setMajorKeyword(command.getMajorKeyword());
        criteria.setMinimumScore(command.getMinimumScore());

        List<ResumeDomainService.ResumeSearchResult> searchResults =
            resumeDomainService.complexSearch(parsedResumes, criteria);

        log.debug("Search returned {} results", searchResults.size());

        // 3. 추가 필터링 (회사, 직책)
        if (command.getCompanyKeyword() != null || command.getPositionKeyword() != null) {
            List<Resume> filteredByCompany = resumeDomainService.searchByExperience(
                searchResults.stream().map(ResumeDomainService.ResumeSearchResult::getResume).collect(Collectors.toList()),
                command.getCompanyKeyword(),
                command.getPositionKeyword(),
                null
            );

            // 점수 유지하면서 필터링
            searchResults = searchResults.stream()
                .filter(result -> filteredByCompany.contains(result.getResume()))
                .collect(Collectors.toList());
        }

        // 4. 페이징 처리
        int pageNumber = command.getPageNumber();
        int pageSize = command.getPageSize();
        int fromIndex = pageNumber * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, searchResults.size());

        if (fromIndex >= searchResults.size()) {
            return List.of();
        }

        List<ResumeDomainService.ResumeSearchResult> pagedResults =
            searchResults.subList(fromIndex, toIndex);

        // 5. DTO 변환
        return pagedResults.stream()
            .map(result -> ResumeResult.withScore(
                result.getResume(),
                result.getScore()
            ))
            .collect(Collectors.toList());
    }

    /**
     * 이력서 삭제 (소프트 삭제)
     *
     * @param resumeId 이력서 ID
     * @param userId 사용자 ID (권한 검증용)
     */
    @Transactional
    public void deleteResume(Long resumeId, Long userId) {
        log.info("Deleting resume: resumeId={}, userId={}", resumeId, userId);

        ResumeId id = ResumeId.of(resumeId);
        UserId uid = UserId.of(userId);

        Resume resume = resumeRepository.findByIdAndUserId(id, uid)
            .orElseThrow(() -> new IllegalArgumentException("이력서를 찾을 수 없습니다"));

        // 도메인 메서드를 통한 삭제 (비즈니스 규칙 검증 포함)
        resume.delete(uid);

        // 저장 (소프트 삭제 상태 반영 및 이벤트 발행)
        resumeRepository.save(resume);

        log.info("Resume deleted successfully: resumeId={}", resumeId);
    }

    /**
     * 이력서 재파싱
     *
     * 파싱에 실패했거나 재파싱이 필요한 경우 사용합니다.
     *
     * @param resumeId 이력서 ID
     * @param userId 사용자 ID (권한 검증용)
     * @return 업데이트된 이력서 정보
     */
    @Transactional
    public ResumeResult reparseResume(Long resumeId, Long userId) {
        log.info("Reparsing resume: resumeId={}, userId={}", resumeId, userId);

        ResumeId id = ResumeId.of(resumeId);
        UserId uid = UserId.of(userId);

        Resume resume = resumeRepository.findByIdAndUserId(id, uid)
            .orElseThrow(() -> new IllegalArgumentException("이력서를 찾을 수 없습니다"));

        // 재파싱 요청 (도메인 메서드)
        resume.requestReparse();

        // 저장 (상태 변경 및 이벤트 발행)
        Resume updatedResume = resumeRepository.save(resume);

        log.info("Resume reparse requested: resumeId={}", resumeId);

        return ResumeResult.from(updatedResume);
    }

    /**
     * 이력서 파싱 완료 처리
     *
     * 파싱 서비스(비동기)가 완료되면 호출되는 메서드입니다.
     * 이벤트 핸들러에서 호출됩니다.
     *
     * @param resumeId 이력서 ID
     * @param parsingResult 파싱 결과
     */
    @Transactional
    public void completeResumeParsing(Long resumeId, ResumeParsingPort.ResumeParsingResult parsingResult) {
        log.info("Completing resume parsing: resumeId={}", resumeId);

        ResumeId id = ResumeId.of(resumeId);
        Resume resume = resumeRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("이력서를 찾을 수 없습니다"));

        if (parsingResult.isSuccess()) {
            // 파싱 성공
            resume.completeParsing(
                parsingResult.getCandidateName(),
                parsingResult.getSkills(),
                parsingResult.getExperiences(),
                parsingResult.getEducations(),
                parsingResult.getContactInfo()
            );
            log.info("Resume parsing completed successfully: resumeId={}", resumeId);
        } else {
            // 파싱 실패
            resume.failParsing(parsingResult.getErrorMessage());
            log.warn("Resume parsing failed: resumeId={}, error={}",
                resumeId, parsingResult.getErrorMessage());
        }

        // 저장 (상태 변경 및 이벤트 발행)
        resumeRepository.save(resume);
    }

    /**
     * 이력서 파일명 변경
     *
     * @param resumeId 이력서 ID
     * @param userId 사용자 ID
     * @param newFileName 새 파일명
     * @return 업데이트된 이력서 정보
     */
    @Transactional
    public ResumeResult renameResume(Long resumeId, Long userId, String newFileName) {
        log.info("Renaming resume: resumeId={}, userId={}, newFileName={}",
            resumeId, userId, newFileName);

        ResumeId id = ResumeId.of(resumeId);
        UserId uid = UserId.of(userId);

        Resume resume = resumeRepository.findByIdAndUserId(id, uid)
            .orElseThrow(() -> new IllegalArgumentException("이력서를 찾을 수 없습니다"));

        // 파일명 변경
        FileName fileName = new FileName(newFileName);
        resume.renameFile(fileName);

        // 저장
        Resume updatedResume = resumeRepository.save(resume);

        log.info("Resume renamed successfully: resumeId={}", resumeId);

        return ResumeResult.from(updatedResume);
    }

    /**
     * 사용자의 이력서 개수 조회
     *
     * @param userId 사용자 ID
     * @return 이력서 개수
     */
    @Transactional(readOnly = true)
    public long countUserResumes(Long userId) {
        UserId uid = UserId.of(userId);
        return resumeRepository.countByUserId(uid);
    }

    /**
     * 이력서 존재 여부 확인
     *
     * @param resumeId 이력서 ID
     * @return 존재하면 true
     */
    @Transactional(readOnly = true)
    public boolean existsResume(Long resumeId) {
        ResumeId id = ResumeId.of(resumeId);
        return resumeRepository.existsById(id);
    }
}
