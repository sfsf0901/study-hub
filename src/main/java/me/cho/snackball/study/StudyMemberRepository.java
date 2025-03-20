package me.cho.snackball.study;

import me.cho.snackball.study.domain.Study;
import me.cho.snackball.study.domain.StudyMember;
import me.cho.snackball.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface StudyMemberRepository extends JpaRepository<StudyMember, Long> {

    StudyMember findByStudyAndUser(Study study, User user);

    Long countByStudyAndActiveTrue(Study study);
}
