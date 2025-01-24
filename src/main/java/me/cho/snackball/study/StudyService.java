package me.cho.snackball.study;

import lombok.RequiredArgsConstructor;
import me.cho.snackball.settings.location.LocationRepository;
import me.cho.snackball.settings.location.domain.Location;
import me.cho.snackball.settings.studyTag.StudyTagService;
import me.cho.snackball.settings.studyTag.domain.StudyTag;
import me.cho.snackball.study.domain.*;
import me.cho.snackball.study.dto.CreateStudyForm;
import me.cho.snackball.study.dto.UpdateStudyForm;
import me.cho.snackball.user.domain.User;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

        List<StudyStudyTag> studyStudyTags = study.getStudyStudyTags();
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
        List<StudyLocation> studyLocations = study.getStudyLocations();
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

    public void updatePublishedStatus(User user, Long studyId, Boolean isPublished) {
        Study study = getStudyToUpdate(user, studyId);
        study.setPublished(isPublished);
        study.setPublishedDate(LocalDateTime.now());
    }

    public void updateRecruitingStatus(User user, Long studyId, Boolean isRecruiting) {
        Study study = getStudyToUpdate(user, studyId);
        study.setRecruiting(isRecruiting);
    }

    public void updateClosedStatus(User user, Long studyId, Boolean isClosed) {
        Study study = getStudyToUpdate(user, studyId);
        study.setClosed(isClosed);
        study.setPublished(false);
        study.setRecruiting(false);
        study.setClosedDate(LocalDateTime.now());
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

    public void enrollmentRequest(Long studyId, User user) {
        Study study = findStudyById(studyId);
        StudyMember studyMember = StudyMember.create(user, study); //TODO 저장되나?
    }

    public void enrollmentCancel(Long studyId, User user) {
        Study study = findStudyById(studyId);
        StudyMember studyMember = studyMemberRepository.findByStudyAndUser(study, user);
        if (!studyMember.isActive()) {
            study.getMembers().remove(studyMember);
            studyMemberRepository.delete(studyMember);
        } else {
            //TODO 적절한 예외 찾거나, 만들기
            throw new IllegalStateException("잘못된 요청입니다.");
        }
    }

    public String enrollmentAccept(Long studyId, Long studyMemberId) {
        Study study = findStudyById(studyId);
        StudyMember studyMember = studyMemberRepository.findById(studyMemberId).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 스터디 멤버입니다: " + studyMemberId));

        if (!studyMember.getStudy().getId().equals(studyId) ) {
            throw new IllegalStateException("잘못된 요청입니다.");
        }
        if (countActiveMember(study) + 1 >= study.getLimitOfEnrollment()) {
            return "모집할 수 있는 회원수를 초과했습니다. 모집 가능 회원수: " + study.getLimitOfEnrollment() + "명";
        }

        studyMember.setActive(true);
        studyMember.setActiveDate(LocalDateTime.now());
        return "가입 요청을 수락했습니다.";
    }

    public void enrollmentReject(Long studyId, Long studyMemberId) {
        StudyMember studyMember = studyMemberRepository.findById(studyMemberId).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 스터디 멤버입니다: " + studyMemberId));
        if (studyMember.getStudy().getId().equals(studyId)) {
            studyMemberRepository.delete(studyMember);
        } else {
            //TODO 적절한 예외 찾거나, 만들기
            throw new IllegalStateException("잘못된 요청입니다.");
        }
    }

    public void withdrawal(Long studyId, User user) {
        Study study = findStudyById(studyId);
        StudyMember studyMember = studyMemberRepository.findByStudyAndUser(study, user);
        studyMemberRepository.delete(studyMember);
    }

    public void forceWithdrawal(Long studyId, Long studyMemberId) {
        StudyMember studyMember = studyMemberRepository.findById(studyMemberId).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 스터디 멤버입니다: " + studyMemberId));
        if (studyMember.getStudy().getId().equals(studyId)) {
            studyMemberRepository.delete(studyMember);
        } else {
            //TODO 적절한 예외 찾거나, 만들기
            throw new IllegalStateException("잘못된 요청입니다.");
        }
    }

    public int countActiveMember(Study study) {
        int count = 0;
        for (StudyMember member : study.getMembers()) {
            if (member.isActive()) {
                count++;
            }
        }
        return count;
    }
}
