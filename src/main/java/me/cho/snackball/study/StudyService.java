package me.cho.snackball.study;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.cho.snackball.global.s3.S3Service;
import me.cho.snackball.location.LocationRepository;
import me.cho.snackball.location.domain.Location;
import me.cho.snackball.location.domain.StudyLocation;
import me.cho.snackball.study.dto.SearchConditions;
import me.cho.snackball.study.event.EnrollmentAcceptEvent;
import me.cho.snackball.studyTag.StudyTagService;
import me.cho.snackball.studyTag.domain.StudyStudyTag;
import me.cho.snackball.studyTag.domain.StudyTag;
import me.cho.snackball.study.domain.*;
import me.cho.snackball.study.dto.CreateStudyForm;
import me.cho.snackball.study.dto.UpdateStudyForm;
import me.cho.snackball.study.event.StudyCreateEvent;
import me.cho.snackball.user.domain.User;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class StudyService {

    private final StudyRepository studyRepository;
    private final StudyQueryRepository studyQueryRepository;
    private final StudyTagService studyTagService;
    private final LocationRepository locationRepository;
    private final StudyManagerRepository studyManagerRepository;
    private final StudyMemberRepository studyMemberRepository;
    private final StudyMemberQueryRepository studyMemberQueryRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final S3Service s3Service;

    private static final String DEFAULT_THUMBNAIL_URL = "https://snackball-static-files.s3.ap-northeast-2.amazonaws.com/default-thumbnail.png";


    public Long createStudy(User user, CreateStudyForm createStudyForm) {

        String updatedDescription = processImagesAndUpload(createStudyForm.getFullDescription());
        String firstImageUrl = extractFirstImageUrl(updatedDescription);
        String thumbnailUrl = firstImageUrl != null ? firstImageUrl : DEFAULT_THUMBNAIL_URL;

        Study study = Study.createStudy(createStudyForm, updatedDescription, thumbnailUrl);
        studyRepository.save(study);

        StudyManager.createStudyManager(user, study);

        createStudyTagsAndStudyStudyTags(createStudyForm.getStudyTags(), study);
        createStudyLocations(createStudyForm.getLocations(), study);

        return study.getId();
    }

    // Summernote 에디터 내용에서 Base64 이미지를 추출하여 S3에 업로드 후 URL로 변경
    private String processImagesAndUpload(String fullDescription) {
        if (fullDescription == null || fullDescription.isEmpty()) {
            return fullDescription;
        }

        Pattern pattern = Pattern.compile("<img[^>]+src=\"(data:image/[^\"]+)\"");
        Matcher matcher = pattern.matcher(fullDescription);

        StringBuffer updatedDescription = new StringBuffer();
        String firstImageUrl = null; // 첫 번째 이미지 URL 저장

        while (matcher.find()) {
            String base64Image = matcher.group(1);
            log.info("########Base64 이미지 발견: {}", base64Image.substring(0, 30) + "...");

            try {
                // S3에 업로드 후 새로운 URL 받기
                String imageUrl = s3Service.uploadBase64Image(base64Image);
                log.info("########S3에 저장 완료: {}", imageUrl);

                // 첫 번째 이미지라면 썸네일로 저장
                if (firstImageUrl == null) {
                    firstImageUrl = imageUrl;
                }

                // 기존 Base64 이미지를 S3 URL로 변경
                matcher.appendReplacement(updatedDescription, "<img src=\"" + imageUrl + "\"");

            } catch (Exception e) {
                log.error("######## Base64 이미지 업로드 실패: {}", e.getMessage());
            }
        }

        matcher.appendTail(updatedDescription);

        return updatedDescription.toString();
    }

    private String extractFirstImageUrl(String fullDescription) {
        Pattern pattern = Pattern.compile("<img[^>]+src=\"(https://[^\"]+)\"");
        Matcher matcher = pattern.matcher(fullDescription);

        if (matcher.find()) {
            return matcher.group(1); // 첫 번째 이미지 URL 반환
        }

        return null; // 이미지가 없으면 null 반환
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
        //TODO 모집된 회원수보다 이하로 수정 못하게 하는 로직 추가하기
        study.setLimitOfEnrollment(updateStudyForm.getLimitOfEnrollment());
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

        eventPublisher.publishEvent(new StudyCreateEvent(study));
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

    public void deleteStudy(User user, Long studyId) {
        Study study = getStudyToUpdate(user, studyId);
        studyRepository.delete(study);
    }

    @Transactional(readOnly = true)
    public Study getStudyToUpdate(User user, Long studyId) {
        Study study = findStudyById(studyId);
        if (!isStudyManager(user, study)) {
            throw new AccessDeniedException("해당 기능은 스터디 관리자만 사용할 수 있습니다.");
        }
        return study;
    }

    @Transactional(readOnly = true)
    public Study findStudyById(Long studyId) {
        return studyRepository.findById(studyId).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 스터디입니다. 스터디 id: " + studyId));

    }

    @Transactional(readOnly = true)
    public boolean isStudyManager(User user, Study study) {
        StudyManager studyManager = studyManagerRepository.findByStudyAndUser(study, user);
        return study.getManagers().contains(studyManager);
    }

    @Transactional(readOnly = true)
    public boolean isStudyMember(User user, Study study) {
        StudyMember studyMember = studyMemberRepository.findByStudyAndUser(study, user);
        return study.getMembers().contains(studyMember);
    }

    @Transactional(readOnly = true)
    public boolean isStudyManagerOrStudyMember(User user, Study study) {
        if (isStudyManager(user, study) || isStudyMember(user, study)) {
            return true;
        } else {
            return false;
        }
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
        StudyMember studyMember = studyMemberQueryRepository.findByIdWithUser(studyMemberId);
        if (studyMember == null) {
            throw  new IllegalArgumentException("존재하지 않는 스터디 멤버입니다: " + studyMemberId);
        }

        if (!studyMember.getStudy().getId().equals(studyId) ) {
            throw new IllegalStateException("잘못된 요청입니다.");
        }
        if (countActiveMember(study) + 1 >= study.getLimitOfEnrollment()) {
            return "모집할 수 있는 회원수를 초과했습니다. 모집 가능 회원수: " + study.getLimitOfEnrollment() + "명";
        }

        studyMember.setActive(true);
        studyMember.setActiveDate(LocalDateTime.now());

        eventPublisher.publishEvent(new EnrollmentAcceptEvent(study, studyMember.getUser()));

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

    @Transactional(readOnly = true)
    public int countActiveMember(Study study) {
        int count = 0;
        for (StudyMember member : study.getMembers()) {
            if (member.isActive()) {
                count++;
            }
        }
        return count;
    }

    @Transactional(readOnly = true)
    public List<Study> findByPublished(SearchConditions searchConditions, int offset, int limit) {
        return studyQueryRepository.findByPublished(searchConditions, offset, limit);
    }

    @Transactional(readOnly = true)
    public long countByPublished(SearchConditions searchConditions) {
        return studyQueryRepository.countByPublished(searchConditions);
    }

    @Transactional(readOnly = true)
    public Page<Study> findByPublishedPage(SearchConditions searchConditions, int offset, int limit) {
        return studyQueryRepository.findByPublishedPage(searchConditions, offset, limit);
    }

    @Transactional(readOnly = true)
    public List<Study> findAll(int offset, int limit) {
        return studyQueryRepository.findAll(offset, limit);
    }
}
