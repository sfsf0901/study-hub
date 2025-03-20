package me.cho.snackball.studyComment;

import me.cho.snackball.studyComment.domain.StudyComment;
import me.cho.snackball.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
public interface StudyCommentRepository extends JpaRepository<StudyComment, Long> {
    List<StudyComment> findByStudyId(Long studyId);

    Long countByUser(User user);
}
