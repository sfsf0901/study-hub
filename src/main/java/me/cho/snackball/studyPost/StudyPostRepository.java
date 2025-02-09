package me.cho.snackball.studyPost;

import me.cho.snackball.studyPost.domain.StudyPost;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyPostRepository extends JpaRepository<StudyPost, Long> {
}
