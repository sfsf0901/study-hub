package me.cho.snackball.studyPost;

import me.cho.snackball.studyPost.domain.StudyPost;
import me.cho.snackball.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyPostRepository extends JpaRepository<StudyPost, Long> {

    Long countByUser(User user);
}
