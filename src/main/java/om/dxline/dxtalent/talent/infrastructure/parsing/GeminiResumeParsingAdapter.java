package om.dxline.dxtalent.talent.infrastructure.parsing;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import om.dxline.dxtalent.common.service.GeminiService;
import om.dxline.dxtalent.common.service.S3UploadService;
import om.dxline.dxtalent.identity.domain.model.Email;
import om.dxline.dxtalent.talent.domain.model.*;
import om.dxline.dxtalent.talent.domain.port.ResumeParsingPort;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Component;

/**
 * Gemini AI Resume Parsing Adapter
 *
 * ResumeParsingPort를 구현한 Gemini AI 기반 이력서 파싱 어댑터입니다.
 * 기존 GeminiService와 TikaDocumentReader를 사용하여 이력서를 파싱합니다.
 *
 * 책임:
 * - 도메인 Port 인터페이스 구현
 * - S3에서 파일 다운로드
 * - Tika로 텍스트 추출
 * - Gemini AI로 파싱
 * - JSON → 도메인 모델 변환
 * - 예외 처리
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GeminiResumeParsingAdapter implements ResumeParsingPort {

    private final GeminiService geminiService;
    private final S3UploadService s3UploadService;
    private final ObjectMapper objectMapper;

    /**
     * 이력서 파일 파싱 (S3에서 다운로드)
     *
     * @param s3Key S3 파일 키
     * @param fileType 파일 타입
     * @return 파싱 결과
     */
    @Override
    public ResumeParsingResult parseResume(String s3Key, FileType fileType) {
        log.info(
            "Starting resume parsing from S3: s3Key={}, fileType={}",
            s3Key,
            fileType
        );

        try {
            // 1. 파일 타입 검증
            if (!canParse(fileType)) {
                return ResumeParsingResult.failure(
                    "지원하지 않는 파일 형식입니다: " + fileType
                );
            }

            // 2. S3에서 파일 다운로드
            String s3Url = constructS3Url(s3Key);
            InputStream inputStream = s3UploadService.downloadFile(s3Url);

            // 3. Tika로 텍스트 추출
            String rawText = extractTextFromFile(inputStream);

            if (rawText == null || rawText.trim().isEmpty()) {
                return ResumeParsingResult.failure(
                    "파일에서 텍스트를 추출할 수 없습니다"
                );
            }

            log.debug("Extracted text length: {} characters", rawText.length());

            // 4. Gemini로 파싱
            return parseText(rawText);
        } catch (Exception e) {
            log.error("Failed to parse resume from S3", e);
            return ResumeParsingResult.failure(
                "파싱 중 오류가 발생했습니다: " + e.getMessage()
            );
        }
    }

    /**
     * 이력서 텍스트 직접 파싱
     *
     * @param rawText 이력서 원문 텍스트
     * @return 파싱 결과
     */
    @Override
    public ResumeParsingResult parseText(String rawText) {
        log.info(
            "Starting resume parsing from text: length={}",
            rawText.length()
        );

        try {
            // 1. Gemini AI로 파싱
            String parsedJson = geminiService.parseResumeText(rawText);

            if (parsedJson == null || parsedJson.trim().isEmpty()) {
                return ResumeParsingResult.failure("AI 파싱에 실패했습니다");
            }

            log.debug("Received parsed JSON from Gemini: {}", parsedJson);

            // 2. JSON → 도메인 모델 변환
            JsonNode json = objectMapper.readTree(parsedJson);

            // 후보자 이름
            CandidateName candidateName = parseCandidateName(json);
            if (candidateName == null) {
                return ResumeParsingResult.failure(
                    "후보자 이름을 파싱할 수 없습니다"
                );
            }

            // 스킬
            List<Skill> skills = parseSkills(json);

            // 경력
            List<Experience> experiences = parseExperiences(json);

            // 학력
            List<Education> educations = parseEducations(json);

            // 연락처
            ContactInfo contactInfo = parseContactInfo(json);

            log.info("Resume parsing completed successfully");

            return ResumeParsingResult.success(
                candidateName,
                skills,
                experiences,
                educations,
                contactInfo,
                rawText
            );
        } catch (Exception e) {
            log.error("Failed to parse resume text", e);
            return ResumeParsingResult.failure(
                "파싱 중 오류가 발생했습니다: " + e.getMessage()
            );
        }
    }

    /**
     * 파일 타입 파싱 가능 여부 확인
     *
     * @param fileType 파일 타입
     * @return 파싱 가능하면 true
     */
    @Override
    public boolean canParse(FileType fileType) {
        return fileType.isSupported();
    }

    // ========== Private Helper Methods ==========

    /**
     * 파일에서 텍스트 추출 (Tika 사용)
     */
    private String extractTextFromFile(InputStream inputStream) {
        try {
            TikaDocumentReader reader = new TikaDocumentReader(
                new InputStreamResource(inputStream)
            );
            return reader
                .get()
                .stream()
                .map(doc -> doc.getText())
                .collect(Collectors.joining("\n"));
        } catch (Exception e) {
            log.error("Failed to extract text from file", e);
            return null;
        }
    }

    /**
     * 후보자 이름 파싱
     */
    private CandidateName parseCandidateName(JsonNode json) {
        try {
            String name = json.path("name").asText(null);
            if (name == null || name.trim().isEmpty()) {
                return null;
            }
            return new CandidateName(name.trim());
        } catch (Exception e) {
            log.warn("Failed to parse candidate name", e);
            return null;
        }
    }

    /**
     * 스킬 목록 파싱
     */
    private List<Skill> parseSkills(JsonNode json) {
        List<Skill> skills = new ArrayList<>();

        try {
            JsonNode skillsNode = json.path("skills");

            if (skillsNode.isArray()) {
                for (JsonNode skillNode : skillsNode) {
                    try {
                        String skillName = null;
                        SkillLevel skillLevel = SkillLevel.INTERMEDIATE; // 기본값
                        Integer years = null;

                        if (skillNode.isTextual()) {
                            // 단순 문자열인 경우
                            skillName = skillNode.asText();
                        } else if (skillNode.isObject()) {
                            // 객체인 경우
                            skillName = skillNode.path("name").asText(null);
                            String levelStr = skillNode
                                .path("level")
                                .asText(null);
                            years = skillNode.path("years").asInt(0);

                            if (years == 0) {
                                years = null;
                            }

                            // 레벨 파싱
                            if (levelStr != null) {
                                try {
                                    skillLevel = SkillLevel.valueOf(
                                        levelStr.toUpperCase()
                                    );
                                } catch (IllegalArgumentException e) {
                                    // 기본값 유지
                                }
                            }
                        }

                        if (skillName != null && !skillName.trim().isEmpty()) {
                            skills.add(
                                new Skill(skillName.trim(), skillLevel, years)
                            );
                        }
                    } catch (Exception e) {
                        log.warn(
                            "Failed to parse skill item: {}",
                            skillNode,
                            e
                        );
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Failed to parse skills array", e);
        }

        return skills;
    }

    /**
     * 경력 목록 파싱
     */
    private List<Experience> parseExperiences(JsonNode json) {
        List<Experience> experiences = new ArrayList<>();

        try {
            JsonNode experiencesNode = json.path("experiences");

            if (experiencesNode.isArray()) {
                for (JsonNode expNode : experiencesNode) {
                    try {
                        String companyName = expNode
                            .path("company")
                            .asText(null);
                        String position = expNode.path("position").asText(null);
                        String startDateStr = expNode
                            .path("startDate")
                            .asText(null);
                        String endDateStr = expNode
                            .path("endDate")
                            .asText(null);
                        String description = expNode
                            .path("description")
                            .asText(null);

                        if (
                            companyName != null &&
                            position != null &&
                            startDateStr != null
                        ) {
                            LocalDate startDate = parseDate(startDateStr);
                            LocalDate endDate = parseDate(endDateStr);

                            if (startDate != null) {
                                experiences.add(
                                    Experience.of(
                                        companyName.trim(),
                                        position.trim(),
                                        startDate,
                                        endDate,
                                        description
                                    )
                                );
                            }
                        }
                    } catch (Exception e) {
                        log.warn(
                            "Failed to parse experience item: {}",
                            expNode,
                            e
                        );
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Failed to parse experiences array", e);
        }

        return experiences;
    }

    /**
     * 학력 목록 파싱
     */
    private List<Education> parseEducations(JsonNode json) {
        List<Education> educations = new ArrayList<>();

        try {
            JsonNode educationsNode = json.path("educations");

            if (educationsNode.isArray()) {
                for (JsonNode eduNode : educationsNode) {
                    try {
                        String schoolName = eduNode.path("school").asText(null);
                        String degree = eduNode.path("degree").asText(null);
                        String major = eduNode.path("major").asText(null);
                        String graduationDateStr = eduNode
                            .path("graduationDate")
                            .asText(null);
                        Double gpa = eduNode.path("gpa").asDouble(0.0);

                        if (gpa == 0.0) {
                            gpa = null;
                        }

                        if (
                            schoolName != null &&
                            degree != null &&
                            major != null &&
                            graduationDateStr != null
                        ) {
                            LocalDate graduationDate = parseDate(
                                graduationDateStr
                            );

                            if (graduationDate != null) {
                                educations.add(
                                    Education.of(
                                        schoolName.trim(),
                                        degree.trim(),
                                        major.trim(),
                                        graduationDate,
                                        gpa
                                    )
                                );
                            }
                        }
                    } catch (Exception e) {
                        log.warn(
                            "Failed to parse education item: {}",
                            eduNode,
                            e
                        );
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Failed to parse educations array", e);
        }

        return educations;
    }

    /**
     * 연락처 정보 파싱
     */
    private ContactInfo parseContactInfo(JsonNode json) {
        try {
            String emailStr = json.path("email").asText(null);
            String phoneStr = json.path("phone").asText(null);
            String address = json.path("address").asText(null);

            Email email = null;
            PhoneNumber phoneNumber = null;

            // 이메일 파싱
            if (emailStr != null && !emailStr.trim().isEmpty()) {
                try {
                    email = new Email(emailStr.trim());
                } catch (Exception e) {
                    log.warn("Invalid email format: {}", emailStr);
                }
            }

            // 전화번호 파싱
            if (phoneStr != null && !phoneStr.trim().isEmpty()) {
                try {
                    phoneNumber = new PhoneNumber(phoneStr.trim());
                } catch (Exception e) {
                    log.warn("Invalid phone number format: {}", phoneStr);
                }
            }

            // 최소한 이메일이나 전화번호 중 하나는 있어야 함
            if (email == null && phoneNumber == null) {
                return null;
            }

            return new ContactInfo(phoneNumber, email, address);
        } catch (Exception e) {
            log.warn("Failed to parse contact info", e);
            return null;
        }
    }

    /**
     * 날짜 문자열 파싱
     */
    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }

        // 여러 날짜 형식 시도
        String[] patterns = {
            "yyyy-MM-dd",
            "yyyy.MM.dd",
            "yyyy/MM/dd",
            "yyyy-MM",
            "yyyy.MM",
            "yyyy/MM",
            "yyyy",
        };

        for (String pattern : patterns) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(
                    pattern
                );

                if (pattern.equals("yyyy")) {
                    // 연도만 있는 경우 1월 1일로 설정
                    int year = Integer.parseInt(dateStr.trim());
                    return LocalDate.of(year, 1, 1);
                } else if (pattern.contains("MM") && !pattern.contains("dd")) {
                    // 년월만 있는 경우 1일로 설정
                    String fullDate = dateStr.trim() + "-01";
                    return LocalDate.parse(
                        fullDate,
                        DateTimeFormatter.ofPattern(pattern + "-dd")
                    );
                } else {
                    return LocalDate.parse(dateStr.trim(), formatter);
                }
            } catch (DateTimeParseException | NumberFormatException e) {
                // 다음 패턴 시도
            }
        }

        log.warn("Failed to parse date: {}", dateStr);
        return null;
    }

    /**
     * S3 키로부터 S3 URL 구성
     */
    private String constructS3Url(String s3Key) {
        // 기존 S3UploadService의 URL 형식을 따름
        // 실제로는 bucketName과 region을 주입받아야 하지만,
        // 간단하게 s3Key를 그대로 사용
        return s3Key;
    }
}
