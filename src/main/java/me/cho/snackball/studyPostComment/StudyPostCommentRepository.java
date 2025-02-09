package me.cho.snackball.studyPostComment;

import me.cho.snackball.studyPostComment.domain.StudyPostComment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyPostCommentRepository extends JpaRepository<StudyPostComment, Long> {
}
