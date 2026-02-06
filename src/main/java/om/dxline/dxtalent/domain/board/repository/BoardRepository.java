package om.dxline.dxtalent.domain.board.repository;

import java.util.List;
import java.util.Optional;
import om.dxline.dxtalent.domain.board.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardRepository extends JpaRepository<Board, Long> {

    Optional<Board> findByCode(String code);

    boolean existsByCode(String code);

    List<Board> findByIsActiveTrueOrderByDisplayOrderAsc();
}
