package om.dxline.dxtalent.api.board.service;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import om.dxline.dxtalent.api.board.dto.BoardResponse;
import om.dxline.dxtalent.api.board.dto.CreateBoardRequest;
import om.dxline.dxtalent.api.board.dto.UpdateBoardRequest;
import om.dxline.dxtalent.domain.board.entity.Board;
import om.dxline.dxtalent.domain.board.repository.BoardRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;

    @Transactional(readOnly = true)
    public List<BoardResponse> getBoards() {
        return boardRepository.findByIsActiveTrueOrderByDisplayOrderAsc()
                .stream()
                .map(BoardResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BoardResponse getBoardByCode(String code) {
        Board board = boardRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시판입니다: " + code));
        return BoardResponse.from(board);
    }

    @Transactional
    public BoardResponse createBoard(CreateBoardRequest request) {
        if (boardRepository.existsByCode(request.getCode())) {
            throw new IllegalArgumentException("이미 존재하는 게시판 코드입니다: " + request.getCode());
        }

        Board board = Board.builder()
                .code(request.getCode())
                .name(request.getName())
                .description(request.getDescription())
                .boardType(request.getBoardType())
                .readPermission(request.getReadPermission())
                .writePermission(request.getWritePermission())
                .icon(request.getIcon())
                .displayOrder(request.getDisplayOrder())
                .build();

        return BoardResponse.from(boardRepository.save(board));
    }

    @Transactional
    public BoardResponse updateBoard(Long id, UpdateBoardRequest request) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시판입니다"));

        board.update(
                request.getName(),
                request.getDescription(),
                request.getReadPermission(),
                request.getWritePermission(),
                request.getIcon(),
                request.getDisplayOrder()
        );

        return BoardResponse.from(board);
    }

    @Transactional
    public void deleteBoard(Long id) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시판입니다"));
        boardRepository.delete(board);
    }
}
