package me.cho.snackball.study;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.cho.snackball.global.security.CurrentUser;
import me.cho.snackball.location.LocationService;
import me.cho.snackball.location.domain.StudyLocation;
import me.cho.snackball.studyTag.StudyTagService;
import me.cho.snackball.studyComment.StudyCommentService;
import me.cho.snackball.studyComment.domain.StudyComment;
import me.cho.snackball.studyComment.dto.CreateStudyCommentForm;
import me.cho.snackball.study.domain.*;
import me.cho.snackball.study.dto.*;
import me.cho.snackball.studyTag.domain.StudyStudyTag;
import me.cho.snackball.user.domain.User;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class StudyController {

    private final StudyService studyService;
    private final StudyMemberRepository studyMemberRepository;
    private final StudyTagService studyTagService;
    private final LocationService locationService;
    private final StudyCommentService studyCommentService;

    @GetMapping("/study/create")
    public String createStudyForm(@CurrentUser User user, Model model) {
        if (user == null) {
            return "redirect:/login";
        }

        model.addAttribute("profileImage", user.getProfileImage());
        model.addAttribute(new CreateStudyForm());
        model.addAttribute("studyTags", studyTagService.getStudyTagNames());
        model.addAttribute("locations", locationService.getLocationNames());

        return "study/createStudy";
    }

    @PostMapping("/study/create")
    public String createStudy(@CurrentUser User user,
                              @Valid CreateStudyForm createStudyForm,
                              BindingResult bindingResult,
                              Model model) {
        if (bindingResult.hasErrors()) {
//            model.addAttribute("profileImage", user.getProfileImage());
            model.addAttribute("studyTags", studyTagService.getStudyTagNames());
            model.addAttribute("locations", locationService.getLocationNames());
            return "study/createStudy";
        }
        Long studyId = studyService.createStudy(user, createStudyForm);
        return "redirect:/study/" + studyId;
    }

    @GetMapping("/study/{studyId}/update")
    public String updateStudyForm(@PathVariable("studyId") Long studyId,
                                @CurrentUser User user,
                                Model model) {
        model.addAttribute("profileImage", user.getProfileImage());

        Study study = studyService.getStudyToUpdate(user, studyId);
        //TODO 서비스에 메서드 만드는게 나을까?
        List<String> studyStudyTagNames = study.getStudyStudyTags().stream().map(StudyStudyTag::getName).toList();
        List<String> studyLocationNames = study.getStudyLocations().stream()
                .sorted(Comparator.comparing(StudyLocation::getProvince).thenComparing(StudyLocation::getCity))
                .map(StudyLocation::toString).toList();
        model.addAttribute("updateStudyForm", new UpdateStudyForm(study, studyStudyTagNames, studyLocationNames));

        model.addAttribute("studyTags", studyTagService.getStudyTagNames());
        model.addAttribute("locations", locationService.getLocationNames());

        return "study/updateStudy";
    }

    @PostMapping("/study/{studyId}/update")
    public String updateStudy(@CurrentUser User user,
                              @PathVariable("studyId") Long studyId,
                              @Valid UpdateStudyForm updateStudyForm,
                              BindingResult bindingResult,
                              Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("studyTags", studyTagService.getStudyTagNames());
            model.addAttribute("locations", locationService.getLocationNames());
            return "study/updateStudy";
        }

        studyService.updateStudy(user, updateStudyForm);
        return "redirect:/study/" + studyId;
    }

    @GetMapping("/study/{studyId}")
    public String viewStudy(@PathVariable("studyId") Long studyId,
                            @CurrentUser User user,
                            Model model) {
        //TODO 예외 만들기
        //TODO 한번에 조회하는 코드 짜기
        Study study = studyService.findStudyById(studyId);
        if (!study.isPublished() && !studyService.isStudyManager(user, study)) {
            throw new IllegalStateException("해당 스터디에 접근 권한이 없습니다. studyId: " + studyId);
        }

        if (user != null) {
            model.addAttribute("profileImage", user.getProfileImage());
        }

        model.addAttribute("study", new ViewStudy(study));

        boolean isManager = studyService.isStudyManager(user, study);
        model.addAttribute("isManager", isManager);

        boolean isMember = studyService.isStudyMember(user, study);
        model.addAttribute("isMember", isMember);

        StudyMember studyMember = studyMemberRepository.findByStudyAndUser(study, user);
        model.addAttribute("studyMember", studyMember);

        int activeMemberCount = studyService.countActiveMember(study);
        model.addAttribute("activeMemberCount", activeMemberCount);

        List<StudyComment> studyComments = studyCommentService.findAll(studyId);
        model.addAttribute("studyComments", studyComments);
        model.addAttribute(new CreateStudyCommentForm());

        return "study/viewStudy";
    }

    @GetMapping("/study/{studyId}/members")
    public String viewStudyMembers(@PathVariable("studyId") Long studyId,
                            @CurrentUser User user,
                            Model model) {
        Study study = studyService.findStudyById(studyId);
        if (!study.isPublished() && !studyService.isStudyManager(user, study)) {
            throw new IllegalStateException("해당 스터디에 접근 권한이 없습니다. studyId: " + studyId);
        }

        model.addAttribute("profileImage", user.getProfileImage());
        model.addAttribute("study", new ViewStudy(study));

        boolean isManager = studyService.isStudyManager(user, study);
        model.addAttribute("isManager", isManager);

        boolean isMember = studyService.isStudyMember(user, study);
        model.addAttribute("isMember", isMember);

        StudyMember studyMember = studyMemberRepository.findByStudyAndUser(study, user);
        model.addAttribute("studyMember", studyMember);

        int activeMemberCount = studyService.countActiveMember(study);
        model.addAttribute("activeMemberCount", activeMemberCount);

        model.addAttribute("managersAndMembers", new ViewManagersAndMembers(study));
        return "study/viewMembers";
    }

    @GetMapping("/study/{studyId}/settings/study")
    public String updateStudyStatusForm(@PathVariable("studyId") Long studyId,
                                  @CurrentUser User user,
                                  Model model) {
        model.addAttribute("profileImage", user.getProfileImage());

        Study study = studyService.getStudyToUpdate(user, studyId);
        model.addAttribute("study", new ViewStudy(study));

        boolean isManager = studyService.isStudyManager(user, study);
        model.addAttribute("isManager", isManager);

        boolean isMember = studyService.isStudyMember(user, study);
        model.addAttribute("isMember", isMember);

        int activeMemberCount = studyService.countActiveMember(study);
        model.addAttribute("activeMemberCount", activeMemberCount);

        model.addAttribute(new UpdateStudyStatusForm(study));
        return "study/viewStudySettings";
    }

    @PostMapping("/study/{studyId}/updatePublished")
    @ResponseBody
    public ResponseEntity<Map<String, String>> updatePublishedStatus(@PathVariable Long studyId,
                                                                     @RequestBody Map<String, Boolean> request,
                                                                     @CurrentUser User user) {
        Boolean isPublished = request.get("published");
        if (isPublished == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid payload"));
        }

        studyService.updatePublishedStatus(user, studyId, isPublished);

        return ResponseEntity.ok(Map.of("message", "스터디 상태가 업데이트되었습니다."));
    }

    @PostMapping("/study/{studyId}/updateRecruiting")
    @ResponseBody
    public ResponseEntity<Map<String, String>> updateRecruitingStatus(@PathVariable Long studyId,
                                                                     @RequestBody Map<String, Boolean> request,
                                                                     @CurrentUser User user) {
        Boolean isRecruiting = request.get("recruiting");
        if (isRecruiting == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid payload"));
        }

        studyService.updateRecruitingStatus(user, studyId, isRecruiting);

        return ResponseEntity.ok(Map.of("message", "스터디 멤버 모집 상태가 업데이트되었습니다."));
    }

    @PostMapping("/study/{studyId}/updateClosed")
    @ResponseBody
    public ResponseEntity<Map<String, String>> updateClosedStatus(@PathVariable Long studyId,
                                                                  @RequestBody Map<String, Boolean> request,
                                                                  @CurrentUser User user) {
        Boolean isClosed = request.get("closed");
        if (isClosed == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid payload"));
        }

        studyService.updateClosedStatus(user, studyId, isClosed);

        return ResponseEntity.ok(Map.of("message", "스터디 종료 상태가 업데이트되었습니다."));
    }
}
