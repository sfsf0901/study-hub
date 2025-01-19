package me.cho.snackball.settings.studyTag;

import lombok.RequiredArgsConstructor;
import me.cho.snackball.settings.studyTag.domain.StudyTag;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class StudyTagService {

    private final StudyTagRepository studyTagRepository;

    public StudyTag createStudyTag(String name) {
        StudyTag studyTag = studyTagRepository.findByName(name).orElse(null);

        if (studyTag == null) {
            studyTag = studyTagRepository.save(StudyTag.builder().name(name).build());
        }
        return studyTag;
    }
}
