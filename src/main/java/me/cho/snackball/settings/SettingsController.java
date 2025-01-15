package me.cho.snackball.settings;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.cho.snackball.domain.StudyTag;
import me.cho.snackball.domain.User;
import me.cho.snackball.domain.UserStudyTag;
import me.cho.snackball.studyTag.StudyTagRepository;
import me.cho.snackball.user.CurrentUser;
import me.cho.snackball.user.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class SettingsController {

    public static final String URL_SETTINGS_PROFILE = "/settings/profile";
    public static final String VIEW_SETTINGS_PROFILE = "settings/profile";
    public static final String URL_SETTINGS_PASSWORD = "/settings/password";
    public static final String VIEW_SETTINGS_PASSWORD = "settings/password";
    public static final String URL_SETTINGS_NOTIFICATIONS = "/settings/notifications";
    public static final String VIEW_SETTINGS_NOTIFICATIONS = "settings/notifications";
    public static final String URL_SETTINGS_STUDY_TAGS = "/settings/studytags";
    public static final String VIEW_SETTINGS_STUDY_TAGS = "settings/studyTags";

    private final UserService userService;
    private final StudyTagRepository studyTagRepository;
    private final ModelMapper modelMapper;
    private final NicknameValidator nicknameValidator;

    @InitBinder("updatePasswordForm")
    public void passwordInitBinder(WebDataBinder binder) {
        binder.addValidators(new PasswordFormValidator());
    }

    @InitBinder("updateProfileForm")
    public void nicknameInitBinder(WebDataBinder binder) {
        binder.addValidators(nicknameValidator);
    }

    @GetMapping(URL_SETTINGS_PROFILE)
    public String updateProfileForm(@CurrentUser User user, Model model) {

        model.addAttribute("updateProfileForm", modelMapper.map(user, UpdateProfileForm.class));
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

    @GetMapping(URL_SETTINGS_PASSWORD)
    public String updatePasswordForm(@CurrentUser User user, Model model) {
        model.addAttribute(new UpdatePasswordForm());
        return VIEW_SETTINGS_PASSWORD;
    }

    @PostMapping(URL_SETTINGS_PASSWORD)
    public String updatePassword(@CurrentUser User user,
                                 @Valid UpdatePasswordForm updatePasswordForm,
                                 BindingResult bindingResult,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return VIEW_SETTINGS_PASSWORD;
        }

        userService.updatePassword(user, updatePasswordForm);
        redirectAttributes.addFlashAttribute("message", "비밀번호를 변경했습니다.");
        return "redirect:" + URL_SETTINGS_PASSWORD;
    }

    @GetMapping(URL_SETTINGS_NOTIFICATIONS)
    public String updateNotificationsForm(@CurrentUser User user, Model model) {
        model.addAttribute(modelMapper.map(user, UpdateNotificationsForm.class));
        return VIEW_SETTINGS_NOTIFICATIONS;
    }

    @PostMapping(URL_SETTINGS_NOTIFICATIONS)
    public String updateNotifications(@CurrentUser User user,
                                      @Valid UpdateNotificationsForm updateNotificationsForm,
                                      BindingResult bindingResult,
                                      Model model,
                                      RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return VIEW_SETTINGS_NOTIFICATIONS;
        }

        userService.updateNotifications(user, updateNotificationsForm);
        redirectAttributes.addFlashAttribute("message", "알림 설정을 변경했습니다.");
        return "redirect:" + URL_SETTINGS_NOTIFICATIONS;
    }

    @GetMapping(URL_SETTINGS_STUDY_TAGS)
    public String updateStudyTagsForm(@CurrentUser User user, Model model) {
//        model.addAttribute(user);
        List<StudyTag> studyTags = studyTagRepository.findAll();
        model.addAttribute("studyTags", studyTags.stream().map(StudyTag::getName).collect(Collectors.toList()));

        Set<UserStudyTag> userStudyTags = userService.getUserStudyTags(user);
        model.addAttribute("userStudyTags", userStudyTags.stream().map(UserStudyTag::getName).collect(Collectors.toList()));
        return VIEW_SETTINGS_STUDY_TAGS;
    }

    @PostMapping(URL_SETTINGS_STUDY_TAGS + "/add")
    @ResponseBody
    public ResponseEntity<Map<String, String>> addTags(@CurrentUser User user,
                                                       @RequestBody UpdateStudyTagsForm updateStudyTagsForm) {
        System.out.println(updateStudyTagsForm.getTagName());
        userService.addStudyTags(user, updateStudyTagsForm);

        // JSON 응답을 반환
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        return ResponseEntity.ok(response);
    }

    @PostMapping(URL_SETTINGS_STUDY_TAGS + "/remove")
    @ResponseBody
    public ResponseEntity<Map<String, String>> removeTags(@CurrentUser User user,
                                                       @RequestBody UpdateStudyTagsForm updateStudyTagsForm) {
        userService.removeStudyTags(user, updateStudyTagsForm);

        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        return ResponseEntity.ok(response);
    }
}
