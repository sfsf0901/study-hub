package me.cho.snackball.study;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.cho.snackball.global.security.CurrentUser;
import me.cho.snackball.settings.location.LocationRepository;
import me.cho.snackball.settings.location.LocationService;
import me.cho.snackball.settings.location.domain.Location;
import me.cho.snackball.settings.location.domain.UserLocation;
import me.cho.snackball.settings.studyTag.StudyTagRepository;
import me.cho.snackball.settings.studyTag.StudyTagService;
import me.cho.snackball.settings.studyTag.domain.StudyTag;
import me.cho.snackball.settings.studyTag.domain.UserStudyTag;
import me.cho.snackball.study.domain.*;
import me.cho.snackball.study.dto.CreateStudyForm;
import me.cho.snackball.study.dto.UpdateStudyForm;
import me.cho.snackball.study.dto.ViewMembers;
import me.cho.snackball.study.dto.ViewStudy;
import me.cho.snackball.user.domain.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class StudyController {

    private final StudyRepository studyRepository;
    private final StudyService studyService;
    private final StudyManagerRepository studyManagerRepository;
    private final StudyMemberRepository studyMemberRepository;
    private final StudyTagRepository studyTagRepository;
    private final LocationRepository locationRepository;
    private final StudyTagService studyTagService;
    private final LocationService locationService;

    @GetMapping("/study/create")
    public String createStudyForm(@CurrentUser User user, Model model) {
        model.addAttribute(new CreateStudyForm());
        model.addAttribute("studyTags", studyTagService.getStudyTagNames());
        model.addAttribute("locations", locationService.getLocationNames());

        return "study/createStudy";
    }

    @PostMapping("/study/create")
    public String createStudy(@CurrentUser User user,
                              @Valid CreateStudyForm createStudyForm,
                              BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "study/createStudy";
        }
        Study study = studyService.createStudy(user, createStudyForm);
        return "redirect:/study/" + study.getId();
    }

    @GetMapping("/study/{studyId}/update")
    public String updateStudyForm(@PathVariable("studyId") Long studyId,
                                @CurrentUser User user,
                                Model model) {
        Study study = studyService.getStudyToUpdate(user, studyId);
        //TODO 서비스에 메서드 만드는게 나을까?
        List<String> studyStudyTagNames = study.getStudyStudyTags().stream().map(StudyStudyTag::getName).toList();
        List<String> studyLocationNames = study.getStudyLocations().stream()
                .sorted(Comparator.comparing(StudyLocation::getProvince).thenComparing(StudyLocation::getCity))
                .map(StudyLocation::toString).toList();
        model.addAttribute(new UpdateStudyForm(study, studyStudyTagNames, studyLocationNames));

        model.addAttribute("studyTags", studyTagService.getStudyTagNames());
        model.addAttribute("locations", locationService.getLocationNames());

        return "study/updateStudy";
    }

    @PostMapping("/study/{studyId}/update")
    public String updateStudy(@CurrentUser User user,
                              @PathVariable("studyId") Long studyId,
                              @Valid UpdateStudyForm updateStudyForm,
                              BindingResult bindingResult,
                              Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("studyTags", studyTagService.getStudyTagNames());
            model.addAttribute("locations", locationService.getLocationNames());
            return "study/updateStudy";
        }

        studyService.updateStudy(user, updateStudyForm);
        return "redirect:/study/" + studyId;
    }

    @GetMapping("/study/{studyId}")
    public String viewStudy(@PathVariable("studyId") Long studyId,
                            @CurrentUser User user,
                            Model model) {
        //TODO 예외 만들기
        //TODO 한번에 조회하는 코드 짜기
        Study study = studyService.findStudyById(studyId);
        model.addAttribute("study", new ViewStudy(study));

        boolean isManager = studyService.isStudyManager(user, study);
        model.addAttribute("isManager", isManager);

        boolean isMember = studyService.isStudyMember(user, study);
        model.addAttribute("isMember", isMember);

        return "study/viewStudy";
    }

    @GetMapping("/study/{studyId}/members")
    public String viewStudyMembers(@PathVariable("studyId") Long studyId,
                            @CurrentUser User user,
                            Model model) {
        Study study = studyRepository.findById(studyId).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 스터디입니다. 스터디 id: " + studyId));
        model.addAttribute("study", new ViewStudy(study));

        StudyManager studyManager = studyManagerRepository.findByStudyAndUser(study, user);
        boolean isManager = study.getManagers().contains(studyManager);
        model.addAttribute("isManager", isManager);

        StudyMember studyMember = studyMemberRepository.findByStudyAndUser(study, user);
        boolean isMember = study.getMembers().contains(studyMember);
        model.addAttribute("isMember", isMember);

        model.addAttribute("members", new ViewMembers(study));
        return "study/viewMembers";
    }
}
