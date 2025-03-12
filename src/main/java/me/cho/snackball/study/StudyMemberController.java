package me.cho.snackball.study;

import lombok.RequiredArgsConstructor;
import me.cho.snackball.global.security.CurrentUser;
import me.cho.snackball.study.domain.Study;
import me.cho.snackball.study.domain.StudyMember;
import me.cho.snackball.study.dto.ViewManagersAndMembers;
import me.cho.snackball.study.dto.ViewStudy;
import me.cho.snackball.user.domain.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class StudyMemberController {

    private final StudyRepository studyRepository;
    private final StudyService studyService;
    private final StudyMemberRepository studyMemberRepository;


    @GetMapping("/study/{studyId}/settings/member")
    public String viewEnrollmentRequest(@PathVariable("studyId") Long studyId,
                                   @CurrentUser User user,
                                   Model model) {
        model.addAttribute("profileImage", user.getProfileImage());

        Study study = studyService.getStudyToUpdate(user, studyId);
        model.addAttribute("study", new ViewStudy(study));

        boolean isManager = studyService.isStudyManager(user, study);
        model.addAttribute("isManager", isManager);

        boolean isMember = studyService.isStudyMember(user, study);
        model.addAttribute("isMember", isMember);

        StudyMember studyMember = studyMemberRepository.findByStudyAndUser(study, user);
        model.addAttribute("studyMember", studyMember);

        int activeMemberCount = studyService.countActiveMember(study);
        model.addAttribute("activeMemberCount", activeMemberCount);
        model.addAttribute("pendingCount", study.getMembers().size() - activeMemberCount);
        model.addAttribute("managersAndMembers", new ViewManagersAndMembers(study));
        return "study/viewMemberSettings";
    }

    @PostMapping("/study/{studyId}/enroll/request")
    public String enrollmentRequest(@PathVariable("studyId") Long studyId, @CurrentUser User user) {
        studyService.enrollmentRequest(studyId, user);
        return "redirect:/study/" + studyId;
    }

    @PostMapping("/study/{studyId}/enroll/cancel")
    public String enrollmentCancel(@PathVariable("studyId") Long studyId, @CurrentUser User user) {
        studyService.enrollmentCancel(studyId, user);
        return "redirect:/study/" + studyId;
    }

    @PostMapping("/study/{studyId}/enroll/accept")
    public String enrollmentAccept(@PathVariable("studyId") Long studyId,
                                   @RequestParam("studyMemberId") Long studyMemberId,
                                   RedirectAttributes redirectAttributes) {
        String result = studyService.enrollmentAccept(studyId, studyMemberId);
        redirectAttributes.addFlashAttribute("result", result);


        return "redirect:/study/" + studyId + "/settings/member";
    }

    @PostMapping("/study/{studyId}/enroll/reject")
    public String enrollmentReject(@PathVariable("studyId") Long studyId,
                                   @RequestParam("studyMemberId") Long studyMemberId,
                                   @CurrentUser User user) {
        studyService.enrollmentReject(studyId, studyMemberId);
        return "redirect:/study/" + studyId + "/settings/member";
    }

    @PostMapping("/study/{studyId}/withdrawal")
    public String withdrawal(@PathVariable("studyId") Long studyId, @CurrentUser User user) {
        studyService.withdrawal(studyId, user);
        return "redirect:/study/" + studyId;
    }

    @PostMapping("/study/{studyId}/forcewithdrawal")
    public String forceWithdrawal(@PathVariable("studyId") Long studyId,
                                  @RequestParam("studyMemberId") Long studyMemberId,
                                  @CurrentUser User user) {
        studyService.forceWithdrawal(studyId, studyMemberId);
        return "redirect:/study/" + studyId + "/settings/member";
    }
}

