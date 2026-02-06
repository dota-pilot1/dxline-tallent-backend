package om.dxline.dxtalent.talent.domain.port;

import java.util.List;
import om.dxline.dxtalent.talent.domain.model.*;

/**
 * Resume 파싱 포트 인터페이스 (Domain Layer)
 *
 * 이력서 파일을 파싱하여 구조화된 정보로 변환하는 기능을 추상화합니다.
 * 실제 구현은 인프라 레이어에서 제공됩니다 (예: Gemini AI, OpenAI 등).
 *
 * 포트 패턴 (Hexagonal Architecture):
 * - 도메인 레이어가 외부 기술에 의존하지 않도록 추상화
 * - 구현체는 인프라 레이어에 위치
 * - 도메인 모델만 사용 (DTO나 인프라 객체 사용 안 함)
 *
 * 사용 예시:
 * <pre>
 * ResumeParsingResult result = resumeParsingPort.parseResume(s3Key, fileType);
 * if (result.isSuccess()) {
 *     resume.completeParsing(
 *         result.getCandidateName(),
 *         result.getSkills(),
 *         result.getExperiences(),
 *         result.getEducations(),
 *         result.getContactInfo()
 *     );
 * }
 * </pre>
 */
public interface ResumeParsingPort {

    /**
     * 이력서 파일 파싱
     *
     * S3에 저장된 이력서 파일을 읽어서 파싱합니다.
     * AI 기반 파싱 서비스를 사용하여 텍스트를 구조화된 정보로 변환합니다.
     *
     * @param s3Key S3 파일 키
     * @param fileType 파일 타입
     * @return 파싱 결과
     */
    ResumeParsingResult parseResume(String s3Key, FileType fileType);

    /**
     * 이력서 텍스트 직접 파싱
     *
     * 이미 추출된 텍스트를 파싱합니다.
     * 테스트나 재파싱 시 유용합니다.
     *
     * @param rawText 이력서 원문 텍스트
     * @return 파싱 결과
     */
    ResumeParsingResult parseText(String rawText);

    /**
     * 파싱 가능 여부 확인
     *
     * 지정된 파일 타입이 파싱 가능한지 확인합니다.
     *
     * @param fileType 파일 타입
     * @return 파싱 가능하면 true
     */
    boolean canParse(FileType fileType);

    /**
     * Resume 파싱 결과
     *
     * 파싱 성공/실패 정보와 추출된 데이터를 담는 VO입니다.
     */
    class ResumeParsingResult {

        private final boolean success;
        private final String errorMessage;
        private final CandidateName candidateName;
        private final List<Skill> skills;
        private final List<Experience> experiences;
        private final List<Education> educations;
        private final ContactInfo contactInfo;
        private final String rawText;

        /**
         * 파싱 성공 결과 생성
         */
        public static ResumeParsingResult success(
            CandidateName candidateName,
            List<Skill> skills,
            List<Experience> experiences,
            List<Education> educations,
            ContactInfo contactInfo,
            String rawText
        ) {
            return new ResumeParsingResult(
                true,
                null,
                candidateName,
                skills,
                experiences,
                educations,
                contactInfo,
                rawText
            );
        }

        /**
         * 파싱 실패 결과 생성
         */
        public static ResumeParsingResult failure(String errorMessage) {
            return new ResumeParsingResult(
                false,
                errorMessage,
                null,
                null,
                null,
                null,
                null,
                null
            );
        }

        private ResumeParsingResult(
            boolean success,
            String errorMessage,
            CandidateName candidateName,
            List<Skill> skills,
            List<Experience> experiences,
            List<Education> educations,
            ContactInfo contactInfo,
            String rawText
        ) {
            this.success = success;
            this.errorMessage = errorMessage;
            this.candidateName = candidateName;
            this.skills = skills;
            this.experiences = experiences;
            this.educations = educations;
            this.contactInfo = contactInfo;
            this.rawText = rawText;
        }

        /**
         * 파싱 성공 여부
         */
        public boolean isSuccess() {
            return success;
        }

        /**
         * 파싱 실패 여부
         */
        public boolean isFailure() {
            return !success;
        }

        /**
         * 에러 메시지 반환
         */
        public String getErrorMessage() {
            return errorMessage;
        }

        /**
         * 후보자 이름 반환
         */
        public CandidateName getCandidateName() {
            return candidateName;
        }

        /**
         * 스킬 목록 반환
         */
        public List<Skill> getSkills() {
            return skills;
        }

        /**
         * 경력 목록 반환
         */
        public List<Experience> getExperiences() {
            return experiences;
        }

        /**
         * 학력 목록 반환
         */
        public List<Education> getEducations() {
            return educations;
        }

        /**
         * 연락처 정보 반환
         */
        public ContactInfo getContactInfo() {
            return contactInfo;
        }

        /**
         * 원문 텍스트 반환
         */
        public String getRawText() {
            return rawText;
        }
    }
}
