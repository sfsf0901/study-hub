package me.cho.snackball.study;

import me.cho.snackball.study.domain.Study;
import me.cho.snackball.study.domain.StudyManager;
import me.cho.snackball.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
public interface StudyManagerRepository extends JpaRepository<StudyManager, Long> {

    List<StudyManager> findByStudy(Study study);

    StudyManager findByStudyAndUser(Study study, User user);
}
