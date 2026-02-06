package om.dxline.dxtalent.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import om.dxline.dxtalent.domain.board.entity.Board;
import om.dxline.dxtalent.domain.board.entity.BoardType;
import om.dxline.dxtalent.domain.board.repository.BoardRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BoardDataInitializer implements CommandLineRunner {

    private final BoardRepository boardRepository;

    @Override
    public void run(String... args) {
        if (boardRepository.count() > 0) {
            log.info("게시판 데이터가 이미 존재합니다. 초기화를 건너뜁니다.");
            return;
        }

        log.info("게시판 초기 데이터를 생성합니다...");

        boardRepository.save(Board.builder()
                .code("free")
                .name("자유게시판")
                .description("자유롭게 의견을 나누는 공간입니다.")
                .boardType(BoardType.GENERAL)
                .readPermission("ALL")
                .writePermission("USER")
                .displayOrder(1)
                .build());

        boardRepository.save(Board.builder()
                .code("notice")
                .name("공지사항")
                .description("중요한 공지사항을 확인하세요.")
                .boardType(BoardType.NOTICE)
                .readPermission("ALL")
                .writePermission("ADMIN")
                .displayOrder(0)
                .build());

        boardRepository.save(Board.builder()
                .code("qna")
                .name("Q&A")
                .description("질문과 답변을 주고받는 공간입니다.")
                .boardType(BoardType.QNA)
                .readPermission("ALL")
                .writePermission("USER")
                .displayOrder(2)
                .build());

        log.info("게시판 초기 데이터 생성 완료 (자유게시판, 공지사항, Q&A)");
    }
}
