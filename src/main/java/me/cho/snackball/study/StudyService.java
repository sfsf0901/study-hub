package me.cho.snackball.study;

import lombok.RequiredArgsConstructor;
import me.cho.snackball.settings.location.LocationRepository;
import me.cho.snackball.settings.location.domain.Location;
import me.cho.snackball.settings.location.domain.UserLocation;
import me.cho.snackball.settings.studyTag.StudyTagService;
import me.cho.snackball.settings.studyTag.domain.StudyTag;
import me.cho.snackball.settings.studyTag.domain.UserStudyTag;
import me.cho.snackball.study.domain.*;
import me.cho.snackball.study.dto.CreateStudyForm;
import me.cho.snackball.study.dto.UpdateStudyForm;
import me.cho.snackball.user.domain.User;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class StudyService {

    private final StudyRepository studyRepository;
    private final StudyTagService studyTagService;
    private final LocationRepository locationRepository;
    private final StudyManagerRepository studyManagerRepository;
    private final StudyMemberRepository studyMemberRepository;


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
            StudyTag studyTag = studyTagService.createOrFindStudyTag(name);
            StudyStudyTag.create(study, studyTag);
        }
    }

    private void createStudyLocations(List<String> locationNames, Study study) {
        for (String name : locationNames) {
            String province = name.substring(0, name.indexOf(" / "));
            String city = name.substring(name.indexOf(" / ") + 3);
            Location location = locationRepository.findByProvinceAndCity(province, city).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 활동 지역 입니다: " + province + ", " + city));
            StudyLocation.create(study, location); //TODO 저장되나??
        }
    }

    public void updateStudy(User user, UpdateStudyForm updateStudyForm) {
        Study study = getStudyToUpdate(user, updateStudyForm.getStudyId());
        study.setTitle(updateStudyForm.getTitle());
        updateStudyTagsAndStudyStudyTags(updateStudyForm.getStudyTags(), study);
        updateStudyLocations(updateStudyForm.getLocations(), study);
        study.setFullDescription(updateStudyForm.getFullDescription());
    }

    private void updateStudyTagsAndStudyStudyTags(List<String> studyTagNames, Study study) {

        Set<StudyStudyTag> studyStudyTags = study.getStudyStudyTags();
        for (String name : studyTagNames) {
            StudyTag studyTag = studyTagService.createOrFindStudyTag(name);

            boolean exists = studyStudyTags.stream()
                    .anyMatch(studyStudyTag -> studyStudyTag.getStudyTag().getId().equals(studyTag.getId()));
            if (!exists) {
                //TODO cascade = CascadeType.ALL 때문에 save 안해 됨
                StudyStudyTag.create(study, studyTag);
            }
        }

        studyStudyTags.removeIf(studyStudyTag ->
                studyTagNames.stream().noneMatch(name ->
                        studyStudyTag.getStudyTag().getName().equals(name)));
    }

    private void updateStudyLocations(List<String> locationNames, Study study) {
        List<Location> foundLocations = new ArrayList<>();
        Set<StudyLocation> studyLocations = study.getStudyLocations();
        for (String name : locationNames) {
            String province = name.substring(0, name.indexOf(" / "));
            String city = name.substring(name.indexOf(" / ") + 3);
            Location location = locationRepository.findByProvinceAndCity(province, city).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 활동 지역 입니다: " + province + ", " + city));
            foundLocations.add(location);

            boolean exists = studyLocations.stream()
                    .anyMatch(studyLocation -> studyLocation.getLocation().getId().equals(location.getId()));
            if (!exists) {
                StudyLocation.create(study, location);
            }
        }

        studyLocations.removeIf(studyLocation ->
                foundLocations.stream().noneMatch(foundLocation ->
                        studyLocation.getLocation().equals(foundLocation)));
    }

    public Study getStudyToUpdate(User user, Long studyId) {
        Study study = findStudyById(studyId);
        if (!isStudyManager(user, study)) {
            throw new AccessDeniedException("해당 기능은 스터디 관리자만 사용할 수 있습니다.");
        }
        return study;
    }

    public Study findStudyById(Long studyId) {
        return studyRepository.findById(studyId).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 스터디입니다. 스터디 id: " + studyId));

    }

    public boolean isStudyManager(User user, Study study) {
        StudyManager studyManager = studyManagerRepository.findByStudyAndUser(study, user);
        return study.getManagers().contains(studyManager);
    }

    public boolean isStudyMember(User user, Study study) {
        StudyMember studyMember = studyMemberRepository.findByStudyAndUser(study, user);
        return study.getMembers().contains(studyMember);
    }
}
