package me.cho.snackball.settings.studyTag;

import me.cho.snackball.settings.studyTag.domain.StudyTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Transactional(readOnly = true)
public interface StudyTagRepository extends JpaRepository<StudyTag, Long> {
    Optional<StudyTag> findByName(String tagName);
}
