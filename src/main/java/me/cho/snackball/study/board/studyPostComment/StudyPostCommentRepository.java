package me.cho.snackball.study.board.studyPostComment;

import me.cho.snackball.study.board.studyPostComment.domain.StudyPostComment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyPostCommentRepository extends JpaRepository<StudyPostComment, Long> {
}
