package me.cho.snackball.study.board;

import me.cho.snackball.study.board.domain.StudyPost;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyPostRepository extends JpaRepository<StudyPost, Long> {
}
