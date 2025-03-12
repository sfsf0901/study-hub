package me.cho.snackball.main;

import lombok.RequiredArgsConstructor;
import me.cho.snackball.global.security.CurrentUser;
import me.cho.snackball.notification.NotificationRepository;
import me.cho.snackball.user.domain.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class MainController {

    @GetMapping("/")
    public String home(@CurrentUser User user, Model model) {
        if(user != null) {
            String profileImage = user.getProfileImage();
            model.addAttribute("profileImage", profileImage);
        }

        return "index";
    }


}
