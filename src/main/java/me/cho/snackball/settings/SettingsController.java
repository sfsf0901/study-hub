package me.cho.snackball.settings;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.cho.snackball.domain.User;
import me.cho.snackball.user.CurrentUser;
import me.cho.snackball.user.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class SettingsController {

    public static final String VIEW_SETTINGS_PROFILE = "settings/profile";
    public static final String URL_SETTINGS_PROFILE = "/settings/profile";

    private final UserService userService;

    @GetMapping(URL_SETTINGS_PROFILE)
    public String updateProfileForm(@CurrentUser User user, Model model) {
        model.addAttribute("updateProfileForm", new UpdateProfileForm(user));
        return VIEW_SETTINGS_PROFILE;
    }

    @PostMapping(URL_SETTINGS_PROFILE)
    public String updateProfile(@CurrentUser User user,
                                @Valid UpdateProfileForm updateProfileForm,
                                BindingResult bindingResult,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
//            model.addAttribute("updateProfileForm", updateProfileForm);
            return VIEW_SETTINGS_PROFILE;
        }

        userService.updateProfile(user, updateProfileForm);
        redirectAttributes.addFlashAttribute("message", "프로필을 수정했습니다.");
        return "redirect:" + URL_SETTINGS_PROFILE;
    }
}
