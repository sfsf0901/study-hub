package me.cho.snackball.main;

import me.cho.snackball.global.security.CurrentUser;
import me.cho.snackball.user.domain.User;
import me.cho.snackball.user.dto.LoginForm;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    @GetMapping("/")
    public String home(@CurrentUser User user, Model model) {
        model.addAttribute("profileImg", user.getProfileImage());
        return "index";
    }


}
