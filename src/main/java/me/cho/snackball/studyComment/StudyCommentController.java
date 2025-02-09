package me.cho.snackball.study.studyComment;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.cho.snackball.global.security.CurrentUser;
import me.cho.snackball.study.studyComment.dto.CreateStudyCommentForm;
import me.cho.snackball.user.domain.User;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class StudyCommentController {

    private final StudyCommentService studyCommentService;

    @PostMapping("/study/{studyId}/comment")
    public String createStudyComment(@PathVariable("studyId") Long studyId,
                                     @Valid CreateStudyCommentForm form,
                                     BindingResult bindingResult,
                                     @CurrentUser User user,
                                     RedirectAttributes redirectAttributes) {
        if (user == null) {
            return "redirect:/login";
        }

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errors", bindingResult.getAllErrors());
            return "redirect:/study/{studyId}";
        }

        studyCommentService.createComment(studyId, user, form);
        return "redirect:/study/" + studyId;
    }

    @PostMapping("/study/{studyId}/comment/{commentId}/update")
    public String updateStudyComment(@PathVariable("studyId") Long studyId,
                                     @PathVariable("commentId") Long commentId,
                                     @Valid CreateStudyCommentForm form,
                                     BindingResult bindingResult,
                                     @CurrentUser User user) {
        if (user == null) {
            return "redirect:/login";
        }

        if (bindingResult.hasErrors()) {
            return "redirect:/study/" + studyId;
        }

        studyCommentService.updateComment(studyId, commentId, user, form);
        return "redirect:/study/" + studyId;
    }

    @PostMapping("/study/{studyId}/comment/{commentId}/delete")
    public String deleteStudyComment(@PathVariable("studyId") Long studyId,
                                     @PathVariable("commentId") Long commentId,
                                     @CurrentUser User user) {
        if (user == null) {
            return "redirect:/login";
        }

        studyCommentService.deleteStudyComment(commentId, user);
        return "redirect:/study/" + studyId;
    }
}
