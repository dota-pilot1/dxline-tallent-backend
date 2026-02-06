package om.dxline.dxtalent.api.board.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import om.dxline.dxtalent.api.board.dto.BoardResponse;
import om.dxline.dxtalent.api.board.dto.CreateBoardRequest;
import om.dxline.dxtalent.api.board.dto.UpdateBoardRequest;
import om.dxline.dxtalent.api.board.service.BoardService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Board", description = "게시판 API")
@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    @Operation(summary = "게시판 목록 조회")
    @GetMapping
    public ResponseEntity<List<BoardResponse>> getBoards() {
        return ResponseEntity.ok(boardService.getBoards());
    }

    @Operation(summary = "게시판 상세 조회 (code)")
    @GetMapping("/{code}")
    public ResponseEntity<BoardResponse> getBoard(@PathVariable String code) {
        return ResponseEntity.ok(boardService.getBoardByCode(code));
    }

    @Operation(summary = "게시판 생성 (관리자)")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BoardResponse> createBoard(@Valid @RequestBody CreateBoardRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(boardService.createBoard(request));
    }

    @Operation(summary = "게시판 수정 (관리자)")
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BoardResponse> updateBoard(@PathVariable Long id,
                                                      @Valid @RequestBody UpdateBoardRequest request) {
        return ResponseEntity.ok(boardService.updateBoard(id, request));
    }

    @Operation(summary = "게시판 삭제 (관리자)")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteBoard(@PathVariable Long id) {
        boardService.deleteBoard(id);
        return ResponseEntity.noContent().build();
    }
}
