package me.cho.snackball.studyPostComment;

import me.cho.snackball.studyPostComment.domain.StudyPostComment;
import me.cho.snackball.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyPostCommentRepository extends JpaRepository<StudyPostComment, Long> {
    Long countByUser(User user);
}
