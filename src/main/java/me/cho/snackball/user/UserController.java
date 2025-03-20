package me.cho.snackball.user;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.cho.snackball.study.StudyQueryRepository;
import me.cho.snackball.study.domain.Study;
import me.cho.snackball.studyComment.StudyCommentRepository;
import me.cho.snackball.studyPost.StudyPostRepository;
import me.cho.snackball.studyPostComment.StudyPostCommentRepository;
import me.cho.snackball.user.domain.User;
import me.cho.snackball.global.security.CurrentUser;
import me.cho.snackball.user.dto.EmailLoginForm;
import me.cho.snackball.user.dto.LoginForm;
import me.cho.snackball.user.dto.Profile;
import me.cho.snackball.user.dto.SignupForm;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final SignUpFormValidator signUpFormValidator;
    private final UserService userService;
    private final UserRepository userRepository;
    private final StudyQueryRepository studyQueryRepository;
    private final StudyPostRepository studyPostRepository;
    private final StudyCommentRepository studyCommentRepository;
    private final StudyPostCommentRepository studyPostCommentRepository;


    @InitBinder("signUpForm")
    public void initBinder(WebDataBinder binder) {
        binder.addValidators(signUpFormValidator);
    }

    @GetMapping("/signup")
    public String signUpForm(Model model) {
        model.addAttribute(new SignupForm());
        return "user/signup";
    }

    @PostMapping("/signup")
    public String signUp(@Valid @ModelAttribute SignupForm signupForm, BindingResult bindingResult, Model model, HttpServletRequest request) {
        if (bindingResult.hasErrors()) {
            model.addAttribute(signupForm);
            return "user/signup";
        }

//        User user = userService.processNewUser(signupForm);
        User user = userService.saveUser(signupForm);
        userService.sendSignUpConfirmEmail(user);
        userService.login(user, request);
        return "redirect:/";
    }

    @GetMapping("/checkemailtoken")
    public String checkEmailToken(String token, String email, Model model, HttpServletRequest request) {
        String view = "user/checkedEmail";

        User user = userRepository.findByUsername(email).orElse(null);
        if (user == null) {
            model.addAttribute("error", "wrong.email");
            return view;
        }
        if (!user.isValidToken(token)) {
            model.addAttribute("error", "wrong.token");
            return view;
        }

        userService.completeSignUp(user, request);

        model.addAttribute("numberOfUser", userRepository.count());
        model.addAttribute("nickname", user.getNickname());
        return view;
    }

    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute(new LoginForm());
        return "user/login";
    }

    @GetMapping("/emaillogin")
    public String emailLoginForm(Model model) {
        model.addAttribute("emailLoginForm", new EmailLoginForm());
        return "user/emailLogin";
    }

    @PostMapping("/emaillogin")
    public String emailLoginLink(@Valid EmailLoginForm emailLoginForm, BindingResult bindingResult, Model model, RedirectAttributes attributes) {
        User user = userRepository.findByUsername(emailLoginForm.getUsername()).orElse(null);
        if (user == null) {
            model.addAttribute("error", "존재하지 않는 회원입니다.");
            return "user/emailLogin";
        }

        userService.sendLoginLink(user);
        attributes.addFlashAttribute("message", "로그인 링크를 이메일로 발송했습니다.");
        return "redirect:/emaillogin";
    }

    @GetMapping("/loginbyemail")
    public String loginByEmail(String token, String email, Model model, HttpServletRequest request) {
        String view = "user/loggedInByEmail";

        User user = userRepository.findByUsername(email).orElse(null);
        if (user == null || !user.isValidToken(token)) {
            model.addAttribute("error", "로그인할 수 없습니다.");
            return view;
        }

        userService.login(user, request);
//        userService.deleteToken(user);
        return view;
    }

    @GetMapping("/profile/{nickname}")
    public String viewProfile(@PathVariable String nickname, Model model, @CurrentUser User user) {
        User findUser = userRepository.findByNickname(nickname).orElseThrow(() -> new IllegalArgumentException(nickname + "에 해당하는 사용자가 없습니다."));

        model.addAttribute("profileImage", findUser.getProfileImage());
        model.addAttribute("profile", new Profile(findUser));
        model.addAttribute("isOwner", findUser.equals(user));

        long countCreatedStudy = studyQueryRepository.countByPublishedAndManager(findUser);
        long countParticipatingStudy = studyQueryRepository.countByPublishedAndMember(findUser);
        Long countStudyPost = studyPostRepository.countByUser(findUser);
        Long countStudyComment = studyCommentRepository.countByUser(findUser);
        Long countStudyPostComment = studyPostCommentRepository.countByUser(findUser);

        model.addAttribute("countCreatedStudy", countCreatedStudy);
        model.addAttribute("countParticipatingStudy", countParticipatingStudy);
        model.addAttribute("countStudyPost", countStudyPost);
        model.addAttribute("countComment", countStudyComment + countStudyPostComment);

        return "user/viewProfile";
    }

    @GetMapping("/profile/{nickname}/created")
    public String viewProfileWithCreatedStudy(@PathVariable String nickname,
                                              @RequestParam(value = "offset", defaultValue = "0") int offset,
                                              @RequestParam(value = "limit", defaultValue = "9") int limit,
                                              Model model, @CurrentUser User user) {
        User findUser = userRepository.findByNickname(nickname).orElseThrow(() -> new IllegalArgumentException(nickname + "에 해당하는 사용자가 없습니다."));

        boolean isOwner = findUser.equals(user);
        model.addAttribute("profileImage", findUser.getProfileImage());
        model.addAttribute("profile", new Profile(findUser));
        model.addAttribute("isOwner", isOwner);

        List<Study> studies;
        Long totalElements;
        if (isOwner) {
            studies = studyQueryRepository.findByManager(findUser, offset, limit);
            totalElements = studyQueryRepository.countByManager(findUser);
        } else {
            studies = studyQueryRepository.findByPublishedAndManager(findUser, offset, limit);
            totalElements = studyQueryRepository.countByPublishedAndManager(findUser);
        }

        int currentPage = (offset / limit) + 1;
        int totalPages = (int) Math.ceil((double) totalElements / limit);

        model.addAttribute("studies", studies);
        model.addAttribute("countStudy", totalElements);
        model.addAttribute("currentPage", currentPage);  // 현재 페이지
        model.addAttribute("totalPages", totalPages);  // 전체 페이지 수
        model.addAttribute("limit", limit);

        return "user/viewProfileWithCreatedStudy";
    }

    @GetMapping("/profile/{nickname}/participating")
    public String viewProfileWithParticipatingStudy(@PathVariable String nickname,
                                              @RequestParam(value = "offset", defaultValue = "0") int offset,
                                              @RequestParam(value = "limit", defaultValue = "9") int limit,
                                              Model model, @CurrentUser User user) {
        User findUser = userRepository.findByNickname(nickname).orElseThrow(() -> new IllegalArgumentException(nickname + "에 해당하는 사용자가 없습니다."));

        boolean isOwner = findUser.equals(user);
        model.addAttribute("profileImage", findUser.getProfileImage());
        model.addAttribute("profile", new Profile(findUser));
        model.addAttribute("isOwner", isOwner);

        List<Study> studies = studyQueryRepository.findByPublishedAndMember(findUser, offset, limit);
        Long totalElements = studyQueryRepository.countByPublishedAndMember(findUser);

        int currentPage = (offset / limit) + 1;
        int totalPages = (int) Math.ceil((double) totalElements / limit);

        model.addAttribute("studies", studies);
        model.addAttribute("countStudy", totalElements);
        model.addAttribute("currentPage", currentPage);  // 현재 페이지
        model.addAttribute("totalPages", totalPages);  // 전체 페이지 수
        model.addAttribute("limit", limit);

        return "user/viewProfileWithParticipatingStudy";
    }
}
