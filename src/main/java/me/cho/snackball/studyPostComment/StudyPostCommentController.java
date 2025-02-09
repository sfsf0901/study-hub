package me.cho.snackball.studyPostComment;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.cho.snackball.global.security.CurrentUser;
import me.cho.snackball.studyPostComment.dto.CreateStudyPostCommentForm;
import me.cho.snackball.user.domain.User;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class StudyPostCommentController {

    private final StudyPostCommentService studyPostCommentService;

    @PostMapping("/study/{studyId}/board/{studyPostId}/comment")
    public String createStudyPostComment(@PathVariable("studyId") Long studyId,
                                        @PathVariable("studyPostId") Long studyPostId,
                                        @Valid CreateStudyPostCommentForm form,
                                        BindingResult bindingResult,
                                        @CurrentUser User user,
                                        RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errors", bindingResult.getAllErrors());
            return "redirect:/study/{studyId}/board/{studyPostId}";
        }

        studyPostCommentService.createComment(studyPostId, user, form);
        return "redirect:/study/{studyId}/board/{studyPostId}";
    }

    @PostMapping("/study/{studyId}/board/{studyPostId}/comment/{commentId}/update")
    public String updateStudyComment(@PathVariable("studyId") Long studyId,
                                     @PathVariable("studyPostId") Long studyPostId,
                                     @PathVariable("commentId") Long commentId,
                                     @Valid CreateStudyPostCommentForm form,
                                     BindingResult bindingResult,
                                     @CurrentUser User user) {
        if (bindingResult.hasErrors()) {
            return "redirect:/study/{studyId}/board/{studyPostId}";
        }

        studyPostCommentService.updateComment(studyPostId, commentId, user, form);
        return "redirect:/study/{studyId}/board/{studyPostId}";
    }

    @PostMapping("/study/{studyId}/board/{studyPostId}/comment/{commentId}/delete")
    public String deleteStudyComment(@PathVariable("studyId") Long studyId,
                                     @PathVariable("studyPostId") Long studyPostId,
                                     @PathVariable("commentId") Long commentId,
                                     @CurrentUser User user) {
        studyPostCommentService.deleteStudyComment(commentId, user);
        return "redirect:/study/{studyId}/board/{studyPostId}";
    }
}
