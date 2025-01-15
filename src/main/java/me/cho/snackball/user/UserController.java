package me.cho.snackball.user;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.cho.snackball.domain.User;
import me.cho.snackball.repository.UserRepository;
import me.cho.snackball.user.dto.Profile;
import me.cho.snackball.user.dto.SignupForm;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final SignUpFormValidator signUpFormValidator;
    private final UserService userService;
    private final UserRepository userRepository;


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

        User user = userService.processNewAccount(signupForm);
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

    @GetMapping("/profile/{nickname}")
    public String viewProfile(@PathVariable String nickname, Model model, @CurrentUser User user) {
        User findUser = userRepository.findByNickname(nickname).orElseThrow(() -> new IllegalArgumentException(nickname + "에 해당하는 사용자가 없습니다."));

        model.addAttribute("profile", new Profile(findUser));
        model.addAttribute("isOwner", findUser.equals(user));
        return "user/profileView";
    }

}
