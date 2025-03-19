package me.cho.snackball.settings;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.cho.snackball.location.LocationRepository;
import me.cho.snackball.location.LocationService;
import me.cho.snackball.location.UpdateLocationForm;
import me.cho.snackball.location.UserLocationRepository;
import me.cho.snackball.location.domain.Location;
import me.cho.snackball.location.domain.UserLocation;
import me.cho.snackball.settings.password.PasswordFormValidator;
import me.cho.snackball.settings.profile.NicknameValidator;
import me.cho.snackball.studyTag.StudyTagRepository;
import me.cho.snackball.studyTag.StudyTagService;
import me.cho.snackball.studyTag.UpdateStudyTagsForm;
import me.cho.snackball.studyTag.UserStudyTagRepository;
import me.cho.snackball.studyTag.domain.UserStudyTag;
import me.cho.snackball.user.domain.User;
import me.cho.snackball.global.security.CurrentUser;
import me.cho.snackball.settings.notification.UpdateNotificationsForm;
import me.cho.snackball.settings.password.UpdatePasswordForm;
import me.cho.snackball.settings.profile.UpdateProfileForm;
import me.cho.snackball.user.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class SettingsController {

    public static final String URL_PROFILE = "/settings/profile";
    public static final String VIEW_PROFILE = "settings/profile";
    public static final String URL_PASSWORD = "/settings/password";
    public static final String VIEW_PASSWORD = "settings/password";
    public static final String URL_NOTIFICATIONS = "/settings/notifications";
    public static final String VIEW_NOTIFICATIONS = "settings/notifications";
    public static final String URL_STUDY_TAGS = "/settings/studytags";
    public static final String VIEW_STUDY_TAGS = "settings/studyTags";
    public static final String URL_LOCATIONS = "/settings/locations";
    public static final String VIEW_LOCATIONS = "settings/locations";


    private final UserService userService;
    private final StudyTagRepository studyTagRepository;
    private final UserStudyTagRepository userStudyTagRepository;
    private final StudyTagService studyTagService;
    private final LocationRepository locationRepository;
    private final UserLocationRepository userLocationRepository;
    private final LocationService locationService;
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


    @GetMapping(URL_PROFILE)
    public String updateProfileForm(@CurrentUser User user, Model model) {

        model.addAttribute("profileImage", user.getProfileImage());
        model.addAttribute("updateProfileForm", new UpdateProfileForm(user));
        return VIEW_PROFILE;
    }

    @PostMapping(URL_PROFILE)
    public String updateProfile(@CurrentUser User user,
                                @Valid UpdateProfileForm updateProfileForm,
                                BindingResult bindingResult,
                                Model model,
                                RedirectAttributes redirectAttributes,
                                HttpServletRequest request) {
        if (bindingResult.hasErrors()) {
            return VIEW_PROFILE;
        }

        userService.updateProfile(user, updateProfileForm, request);
        redirectAttributes.addFlashAttribute("message", "프로필을 수정했습니다.");
        return "redirect:" + URL_PROFILE;
    }

    @GetMapping(URL_PASSWORD)
    public String updatePasswordForm(@CurrentUser User user, Model model) {
        model.addAttribute("profileImage", user.getProfileImage());
        model.addAttribute(new UpdatePasswordForm());
        return VIEW_PASSWORD;
    }

    @PostMapping(URL_PASSWORD)
    public String updatePassword(@CurrentUser User user,
                                 @Valid UpdatePasswordForm updatePasswordForm,
                                 BindingResult bindingResult,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return VIEW_PASSWORD;
        }

        userService.updatePassword(user, updatePasswordForm);
        redirectAttributes.addFlashAttribute("message", "비밀번호를 변경했습니다.");
        return "redirect:" + URL_PASSWORD;
    }

    @GetMapping(URL_NOTIFICATIONS)
    public String updateNotificationsForm(@CurrentUser User user, Model model) {
        model.addAttribute("profileImage", user.getProfileImage());
        model.addAttribute(modelMapper.map(user, UpdateNotificationsForm.class));
        return VIEW_NOTIFICATIONS;
    }

    @PostMapping(URL_NOTIFICATIONS)
    public String updateNotifications(@CurrentUser User user,
                                      @Valid UpdateNotificationsForm updateNotificationsForm,
                                      BindingResult bindingResult,
                                      Model model,
                                      RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return VIEW_NOTIFICATIONS;
        }

        userService.updateNotifications(user, updateNotificationsForm);
        redirectAttributes.addFlashAttribute("message", "알림 설정을 변경했습니다.");
        return "redirect:" + URL_NOTIFICATIONS;
    }

    @GetMapping(URL_STUDY_TAGS)
    public String updateStudyTagsForm(@CurrentUser User user, Model model) {
        model.addAttribute("profileImage", user.getProfileImage());
        model.addAttribute("studyTags", studyTagService.getStudyTagNames());

        List<UserStudyTag> userStudyTags = userService.getUserStudyTags(user);
        model.addAttribute("userStudyTags", userStudyTags.stream().map(UserStudyTag::getName).collect(Collectors.toList()));
        return VIEW_STUDY_TAGS;
    }

    @PostMapping(URL_STUDY_TAGS + "/add")
    @ResponseBody
    public ResponseEntity<Map<String, String>> addTags(@CurrentUser User user,
                                                       @RequestBody UpdateStudyTagsForm updateStudyTagsForm) {
        userService.addStudyTag(user, updateStudyTagsForm);

        // JSON 응답을 반환
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        return ResponseEntity.ok(response);
    }

    @PostMapping(URL_STUDY_TAGS + "/remove")
    @ResponseBody
    public ResponseEntity<Map<String, String>> removeTags(@CurrentUser User user,
                                                       @RequestBody UpdateStudyTagsForm updateStudyTagsForm) {
        UserStudyTag userStudyTag = userStudyTagRepository.findByUserIdAndName(user.getId(), updateStudyTagsForm.getTagName()).orElse(null);
        if (userStudyTag == null) {
            return ResponseEntity.notFound().build();
        }

        userService.removeUserStudyTag(user, userStudyTag);

        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        return ResponseEntity.ok(response);
    }

    @GetMapping(URL_LOCATIONS)
    public String updateLocationsForm(@CurrentUser User user, Model model) {
        model.addAttribute("profileImage", user.getProfileImage());
        model.addAttribute("locations", locationService.getLocationNames());

        List<UserLocation> userLocations = userService.getUserLocations(user);
        model.addAttribute("userLocations",
                userLocations.stream()
                        .sorted(Comparator.comparing(UserLocation::getProvince).thenComparing(UserLocation::getCity))
                        .map(UserLocation::toString).collect(Collectors.toList()));

        return VIEW_LOCATIONS;
    }

    @PostMapping(URL_LOCATIONS + "/add")
    @ResponseBody
    public ResponseEntity<Map<String, String>> addLocations(@CurrentUser User user,
                                                       @RequestBody UpdateLocationForm updateLocationForm) {
        Location location = locationRepository.findByProvinceAndCity(updateLocationForm.getProvince(), updateLocationForm.getCity()).orElse(null);
        if (location == null) {
            return ResponseEntity.notFound().build();
        }

        userService.addLocation(user, location);

        // JSON 응답을 반환
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        return ResponseEntity.ok(response);
    }

    @PostMapping(URL_LOCATIONS + "/remove")
    @ResponseBody
    public ResponseEntity<Map<String, String>> removeTags(@CurrentUser User user,
                                                          @RequestBody UpdateLocationForm updateLocationForm) {
        UserLocation userLocation = userLocationRepository.findByProvinceAndCity(updateLocationForm.getProvince(), updateLocationForm.getCity()).orElse(null);
        if (userLocation == null) {
            return ResponseEntity.notFound().build();
        }

        userService.removeUserLocation(user, userLocation);

        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        return ResponseEntity.ok(response);
    }
}
