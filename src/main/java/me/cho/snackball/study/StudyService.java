package me.cho.snackball.study;

import lombok.RequiredArgsConstructor;
import me.cho.snackball.settings.location.LocationRepository;
import me.cho.snackball.settings.location.domain.Location;
import me.cho.snackball.settings.studyTag.StudyTagService;
import me.cho.snackball.settings.studyTag.domain.StudyTag;
import me.cho.snackball.study.domain.Study;
import me.cho.snackball.study.domain.StudyLocation;
import me.cho.snackball.study.domain.StudyManager;
import me.cho.snackball.study.domain.StudyStudyTag;
import me.cho.snackball.study.dto.CreateStudyForm;
import me.cho.snackball.user.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class StudyService {

    private final StudyRepository studyRepository;
    private final StudyTagService studyTagService;
    private final LocationRepository locationRepository;


    public Study createStudy(User user, CreateStudyForm createStudyForm) {
        Study study = Study.createStudy(createStudyForm);
        studyRepository.save(study);

        StudyManager.createStudyManager(user, study);

        createStudyTagsAndStudyStudyTags(createStudyForm.getStudyTags(), study);
        createStudyLocations(createStudyForm.getLocations(), study);

        return study;
    }

    private void createStudyTagsAndStudyStudyTags(List<String> studyTagNames, Study study) {
        for (String name : studyTagNames) {
            StudyTag studyTag = studyTagService.createStudyTag(name);
            StudyStudyTag.create(study, studyTag);
        }
    }

    private void createStudyLocations(List<String> locationNames, Study study) {
        for (String name : locationNames) {
            String province = name.substring(0, name.indexOf(" / "));
            String city = name.substring(name.indexOf(" / ") + 3);
            Location location = locationRepository.findByProvinceAndCity(province, city).orElseThrow(()-> new IllegalArgumentException("존재하지 않는 활동 지역 입니다: " + province + ", " + city));
            StudyLocation.create(study, location); //TODO 저장되나??
        }
    }
}
