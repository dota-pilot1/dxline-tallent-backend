package om.dxline.dxtalent.talent.domain.model;

import om.dxline.dxtalent.shared.domain.model.BaseValueObject;

/**
 * 파일 크기 값 객체 (FileSize Value Object)
 *
 * 이력서 파일의 크기를 표현하는 값 객체입니다.
 *
 * 비즈니스 규칙:
 * - 최대 크기: 10MB (10,485,760 bytes)
 * - 최소 크기: 1KB (1,024 bytes)
 * - 음수 불가
 *
 * 사용 예시:
 * <pre>
 * FileSize fileSize = new FileSize(2_097_152L); // 2MB
 * String readable = fileSize.toHumanReadable(); // "2.00 MB"
 * boolean tooLarge = fileSize.exceedsLimit(); // false
 * </pre>
 */
public class FileSize extends BaseValueObject {

    private static final long KB = 1024L;
    private static final long MB = KB * 1024L;
    private static final long MIN_SIZE = KB; // 1KB
    private static final long MAX_SIZE = 10 * MB; // 10MB

    private final Long bytes;

    /**
     * 바이트 크기로 FileSize 생성
     *
     * @param bytes 파일 크기 (바이트)
     * @throws IllegalArgumentException 유효하지 않은 크기인 경우
     */
    public FileSize(Long bytes) {
        this.bytes = requireNonNull(bytes, "파일 크기");
        validateSize(bytes);
    }

    /**
     * 파일 크기 검증
     */
    private void validateSize(Long bytes) {
        // 음수 검증
        validate(
            bytes >= 0,
            "파일 크기는 음수일 수 없습니다"
        );

        // 최소 크기 검증
        validate(
            bytes >= MIN_SIZE,
            String.format("파일 크기가 너무 작습니다. 최소 %s 이상이어야 합니다", formatBytes(MIN_SIZE))
        );

        // 최대 크기 검증
        validate(
            bytes <= MAX_SIZE,
            String.format("파일 크기가 너무 큽니다. 최대 %s 이하여야 합니다", formatBytes(MAX_SIZE))
        );
    }

    /**
     * 바이트 값 반환
     *
     * @return 파일 크기 (바이트)
     */
    public Long getBytes() {
        return bytes;
    }

    /**
     * KB 단위로 반환
     *
     * @return 파일 크기 (KB)
     */
    public double getKiloBytes() {
        return (double) bytes / KB;
    }

    /**
     * MB 단위로 반환
     *
     * @return 파일 크기 (MB)
     */
    public double getMegaBytes() {
        return (double) bytes / MB;
    }

    /**
     * 사람이 읽기 쉬운 형식으로 변환
     *
     * @return 형식화된 문자열 (예: "2.50 MB", "512.00 KB")
     */
    public String toHumanReadable() {
        return formatBytes(bytes);
    }

    /**
     * 바이트를 사람이 읽기 쉬운 형식으로 변환
     */
    private static String formatBytes(long bytes) {
        if (bytes >= MB) {
            return String.format("%.2f MB", (double) bytes / MB);
        } else if (bytes >= KB) {
            return String.format("%.2f KB", (double) bytes / KB);
        } else {
            return String.format("%d bytes", bytes);
        }
    }

    /**
     * 최대 크기를 초과했는지 확인
     *
     * @return 초과하면 true
     */
    public boolean exceedsLimit() {
        return bytes > MAX_SIZE;
    }

    /**
     * 최소 크기 미만인지 확인
     *
     * @return 미만이면 true
     */
    public boolean isTooSmall() {
        return bytes < MIN_SIZE;
    }

    /**
     * 특정 크기보다 큰지 확인
     *
     * @param other 비교할 FileSize
     * @return 더 크면 true
     */
    public boolean isLargerThan(FileSize other) {
        return this.bytes > other.bytes;
    }

    /**
     * 특정 크기보다 작은지 확인
     *
     * @param other 비교할 FileSize
     * @return 더 작으면 true
     */
    public boolean isSmallerThan(FileSize other) {
        return this.bytes < other.bytes;
    }

    /**
     * 두 파일 크기의 합
     *
     * @param other 더할 FileSize
     * @return 합계 FileSize
     */
    public FileSize add(FileSize other) {
        return new FileSize(this.bytes + other.bytes);
    }

    /**
     * 동등성 비교를 위한 컴포넌트
     */
    @Override
    protected Object[] getEqualityComponents() {
        return new Object[] { bytes };
    }

    /**
     * 문자열 표현
     */
    @Override
    public String toString() {
        return toHumanReadable();
    }

    /**
     * 최대 허용 크기 반환
     *
     * @return 최대 크기 (바이트)
     */
    public static long getMaxSize() {
        return MAX_SIZE;
    }

    /**
     * 최소 허용 크기 반환
     *
     * @return 최소 크기 (바이트)
     */
    public static long getMinSize() {
        return MIN_SIZE;
    }
}
