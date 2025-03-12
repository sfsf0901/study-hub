package me.cho.snackball.test.api;

import lombok.RequiredArgsConstructor;
import me.cho.snackball.location.StudyLocationQueryRepository;
import me.cho.snackball.location.domain.Location;
import me.cho.snackball.studyTag.StudyStudyTagQueryRepository;
import me.cho.snackball.studyTag.domain.StudyTag;
import me.cho.snackball.user.UserQueryRepository;
import me.cho.snackball.user.domain.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class TestApiController {

    private final StudyStudyTagQueryRepository studyStudyTagQueryRepository;
    private final StudyLocationQueryRepository studyLocationQueryRepository;
    private final UserQueryRepository userQueryRepository;

    @GetMapping("/test/tag")
    public List<StudyTag> getStudyTags() {
        return studyStudyTagQueryRepository.findStudyTagsByStudyId(2L);
    }

    @GetMapping("/test/user")
    public List<User> getUsers() {
        List<StudyTag> studyTags = studyStudyTagQueryRepository.findStudyTagsByStudyId(2L);
        List<Location> locations = studyLocationQueryRepository.findLocationByStudyId(2L);
        return userQueryRepository.findByStudyTagsAndLocations(studyTags, locations);
    }
}
