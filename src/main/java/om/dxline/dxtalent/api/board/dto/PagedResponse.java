package om.dxline.dxtalent.api.board.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

@Getter
@Builder
public class PagedResponse<T> {
    private List<T> data;
    private Meta meta;

    @Getter
    @Builder
    public static class Meta {
        private long total;
        private int page;
        private int limit;
        private int totalPages;
    }

    public static <T> PagedResponse<T> from(Page<?> page, List<T> data) {
        return PagedResponse.<T>builder()
                .data(data)
                .meta(Meta.builder()
                        .total(page.getTotalElements())
                        .page(page.getNumber() + 1)
                        .limit(page.getSize())
                        .totalPages(page.getTotalPages())
                        .build())
                .build();
    }
}
