package me.cho.snackball.main;

import jakarta.validation.Valid;
import me.cho.snackball.domain.User;
import me.cho.snackball.user.CurrentUser;
import me.cho.snackball.user.dto.LoginForm;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute(new LoginForm());
        return "user/login";
    }
}
