package me.cho.snackball.study;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.cho.snackball.global.security.CurrentUser;
import me.cho.snackball.settings.location.LocationRepository;
import me.cho.snackball.settings.location.domain.Location;
import me.cho.snackball.settings.studyTag.StudyTagRepository;
import me.cho.snackball.settings.studyTag.domain.StudyTag;
import me.cho.snackball.study.domain.Study;
import me.cho.snackball.study.domain.StudyManager;
import me.cho.snackball.study.domain.StudyMember;
import me.cho.snackball.study.dto.CreateStudyForm;
import me.cho.snackball.study.dto.ViewStudy;
import me.cho.snackball.user.domain.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Comparator;
import java.util.List;
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

    @GetMapping("/study/create")
    public String createStudyForm(@CurrentUser User user, Model model) {
        model.addAttribute(new CreateStudyForm());

        List<StudyTag> studyTags = studyTagRepository.findAll();
        model.addAttribute("studyTags", studyTags.stream().map(StudyTag::getName).collect(Collectors.toList()));

        List<Location> locations = locationRepository.findAll();
        model.addAttribute("locations",
                locations.stream()
                        .sorted(Comparator.comparing(Location::getProvince).thenComparing(Location::getCity))
                        .map(Location::toString)
                        .collect(Collectors.toList()));

        return "study/createStudy";
    }

    @PostMapping("/study/create")
    public String createStudy(@CurrentUser User user,
                              @Valid CreateStudyForm createStudyForm,
                              BindingResult bindingResult,
                              Model model) {
        if (bindingResult.hasErrors()) {
            return "study/createStudy";
        }
        Study study = studyService.createStudy(user, createStudyForm);
        return "redirect:/study/" + study.getId();
    }

    @GetMapping("/study/{studyId}")
    public String viewStudy(@PathVariable("studyId") Long studyId,
                            @CurrentUser User user,
                            Model model) {
        //TODO 예외 만들기
        //TODO 한번에 조회하는 코드 짜기
        Study study = studyRepository.findById(studyId).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 스터디입니다. 스터디 id: " + studyId));
        model.addAttribute("study", new ViewStudy(study));

        StudyManager studyManager = studyManagerRepository.findByStudyAndUser(study, user);
        boolean isManager = study.getManagers().contains(studyManager);
        model.addAttribute("isManager", isManager);

        StudyMember studyMember = studyMemberRepository.findByStudyAndUser(study, user);
        boolean isMember = study.getMembers().contains(studyMember);
        model.addAttribute("isMember", isMember);

        return "study/viewStudy";
    }

//    @GetMapping("/study/{studyId}/members")
//    public String viewStudyMembers(@PathVariable("studyId") Long studyId,
//                            @CurrentUser User user,
//                            Model model) {
//        Study study = studyRepository.findById(studyId).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 스터디입니다. 스터디 id: " + studyId));
//        model.addAttribute("members", study.getMembers());
//    }
}
