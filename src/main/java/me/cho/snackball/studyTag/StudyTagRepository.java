package me.cho.snackball.studyTag;

import me.cho.snackball.domain.StudyTag;
import org.hibernate.validator.constraints.Length;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Transactional(readOnly = true)
public interface StudyTagRepository extends JpaRepository<StudyTag, Long> {
    Optional<StudyTag> findByName(String tagName);
}
