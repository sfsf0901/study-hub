package me.cho.snackball.settings.studyTag;

import me.cho.snackball.settings.studyTag.domain.UserStudyTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Transactional(readOnly = true)
public interface UserStudyTagRepository extends JpaRepository<UserStudyTag, Long> {
    Optional<UserStudyTag> findByName(String tagName);

    Optional<UserStudyTag> findByUserIdAndName(Long userId, String name);
}
