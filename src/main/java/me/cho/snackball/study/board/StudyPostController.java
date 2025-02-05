package me.cho.snackball.study.board;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.cho.snackball.global.security.CurrentUser;
import me.cho.snackball.study.StudyMemberRepository;
import me.cho.snackball.study.StudyService;
import me.cho.snackball.study.board.domain.StudyPost;
import me.cho.snackball.study.board.dto.CreateStudyPostForm;
import me.cho.snackball.study.board.dto.StudyPostDto;
import me.cho.snackball.study.board.dto.UpdateStudyPostForm;
import me.cho.snackball.study.board.postComment.dto.CreatePostCommentForm;
import me.cho.snackball.study.domain.Study;
import me.cho.snackball.study.domain.StudyMember;
import me.cho.snackball.study.dto.ViewStudy;
import me.cho.snackball.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class StudyPostController {

    private final StudyService studyService;
    private final StudyMemberRepository studyMemberRepository;
    private final StudyPostService studyPostService;
    private final StudyPostQueryRepository studyPostQueryRepository;

    @GetMapping("/study/{studyId}/board")
    public String viewStudyBoard(@PathVariable("studyId") Long studyId,
                                 StudyPostSearchCondition condition,
                                 @RequestParam(defaultValue = "1") int page,
                                 @PageableDefault(size = 10) Pageable pageable,
                                 @CurrentUser User user,
                                 Model model) {
        Study study = studyService.findStudyById(studyId);

        // 검증 로직 추가
        if (!studyService.isStudyManagerOrStudyMember(user, study)) {
            throw new IllegalStateException("해당 게시판에 접근 권한이 없습니다. studyId: " + studyId);
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

        Page<StudyPostDto> studyPosts = studyPostQueryRepository.findAllWithUser(studyId, condition, Pageable.ofSize(pageable.getPageSize()).withPage(page - 1))
                .map(StudyPostDto::new);
        model.addAttribute("studyPosts", studyPosts);
        model.addAttribute("studyPostSearchCondition", condition);

        return "study/viewStudyBoard";
    }

    @GetMapping("/study/{studyId}/board/{studyPostId}")
    public String viewStudyPost(@PathVariable("studyId") Long studyId,
                                @PathVariable Long studyPostId,
                                @CurrentUser User user,
                                Model model) {
        Study study = studyService.findStudyById(studyId);
        if (!studyService.isStudyManagerOrStudyMember(user, study)) {
            throw new IllegalStateException("해당 게시물에 접근 권한이 없습니다. studyPostId: " + studyPostId);
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

        StudyPost studyPost = studyPostService.findStudyPost(studyPostId);
        model.addAttribute("studyPost", studyPost);

        model.addAttribute(new CreatePostCommentForm());

        return "study/viewStudyPost";
    }

    @GetMapping("/study/{studyId}/board/createpost")
    public String createStudyPostForm(@PathVariable("studyId") Long studyId,
                                      @CurrentUser User user,
                                      Model model) {

        Study study = studyService.findStudyById(studyId);

        // 검증 로직 추가
        if (!studyService.isStudyManagerOrStudyMember(user, study)) {
            throw new IllegalStateException("해당 게시판에 접근 권한이 없습니다. studyId: " + studyId);
        }

        model.addAttribute("profileImage", user.getProfileImage());
        model.addAttribute("study", new ViewStudy(study));

        model.addAttribute(new CreateStudyPostForm());

        return "study/createStudyPost";
    }

    @PostMapping("/study/{studyId}/board/createpost")
    public String createStudyPost(@PathVariable("studyId") Long studyId,
                                      @CurrentUser User user,
                                      @Valid CreateStudyPostForm createStudyPostForm,
                                      BindingResult bindingResult,
                                      Model model) {

        Study study = studyService.findStudyById(studyId);

        // 검증 로직 추가
        if (!studyService.isStudyManagerOrStudyMember(user, study)) {
            throw new IllegalStateException("해당 게시판에 접근 권한이 없습니다. studyId: " + studyId);
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("profileImage", user.getProfileImage());
            model.addAttribute("study", new ViewStudy(study));
            return "study/createStudyPost";
        }

        Long studyPostId = studyPostService.createStudyPost(user, study, createStudyPostForm);

        return "redirect:/study/" + studyId + "/board/" + studyPostId;
    }

    @GetMapping("/study/{studyId}/board/{studyPostId}/update")
    public String updateStudyPostForm(@PathVariable("studyId") Long studyId,
                                      @PathVariable Long studyPostId,
                                      @CurrentUser User user,
                                      Model model) {
        Study study = studyService.findStudyById(studyId);
        if (!studyService.isStudyManagerOrStudyMember(user, study)) {
            throw new IllegalStateException("해당 게시물에 접근 권한이 없습니다. studyPostId: " + studyPostId);
        }

        model.addAttribute("profileImage", user.getProfileImage());
        model.addAttribute("study", new ViewStudy(study));

        StudyPost studyPost = studyPostService.findStudyPost(studyPostId);
        model.addAttribute("updateStudyPostForm", new UpdateStudyPostForm(studyPost));

        return "study/updateStudyPost";
    }

    @PostMapping("/study/{studyId}/board/{studyPostId}/update")
    public String updateStudyPost(@PathVariable("studyId") Long studyId,
                                  @PathVariable Long studyPostId,
                                  @CurrentUser User user,
                                  @Valid UpdateStudyPostForm updateStudyPostForm,
                                  BindingResult bindingResult,
                                  Model model) {
        Study study = studyService.findStudyById(studyId);

        // 검증 로직 추가
        if (!studyService.isStudyManagerOrStudyMember(user, study)) {
            throw new IllegalStateException("해당 게시판에 접근 권한이 없습니다. studyId: " + studyId);
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("profileImage", user.getProfileImage());
            model.addAttribute("study", new ViewStudy(study));
            return "study/updateStudyPost";
        }

        studyPostService.updateStudyPost(user, studyPostId, updateStudyPostForm);

        return "redirect:/study/" + studyId + "/board/" + studyPostId;
    }

    @PostMapping("/study/{studyId}/board/{studyPostId}/delete")
    public String updateStudyPost(@PathVariable("studyId") Long studyId,
                                  @PathVariable Long studyPostId,
                                  @CurrentUser User user,
                                  Model model) {
        Study study = studyService.findStudyById(studyId);

        // 검증 로직 추가
        if (!studyService.isStudyManagerOrStudyMember(user, study)) {
            throw new IllegalStateException("해당 게시판에 접근 권한이 없습니다. studyId: " + studyId);
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

        studyPostService.delete(user, studyPostId);

        return "redirect:/study/{studyId}/board";
    }
}
