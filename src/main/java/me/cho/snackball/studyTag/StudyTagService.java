package me.cho.snackball.studyTag;

import lombok.RequiredArgsConstructor;
import me.cho.snackball.studyTag.domain.StudyTag;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class StudyTagService {

    private final StudyTagRepository studyTagRepository;

    public StudyTag createOrFindStudyTag(String name) {
        StudyTag studyTag = studyTagRepository.findByName(name).orElse(null);

        if (studyTag == null) {
            studyTag = studyTagRepository.save(StudyTag.builder().name(name).build());
        }
        return studyTag;
    }

    public List<String> getStudyTagNames() {
        return studyTagRepository.findAll().stream().map(StudyTag::getName).collect(Collectors.toList());
    }
}
