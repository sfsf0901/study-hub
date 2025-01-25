package me.cho.snackball.main;

import me.cho.snackball.global.security.CurrentUser;
import me.cho.snackball.user.domain.User;
import me.cho.snackball.user.dto.LoginForm;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

@Controller
public class MainController {

    @GetMapping("/")
    public String home(@CurrentUser User user, Model model) {
        String profileImage = user.getProfileImage();
        model.addAttribute("profileImage", profileImage);
        return "index";
    }


}
