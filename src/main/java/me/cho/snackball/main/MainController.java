package me.cho.snackball.main;

import lombok.RequiredArgsConstructor;
import me.cho.snackball.global.security.CurrentUser;
import me.cho.snackball.study.StudyMemberRepository;
import me.cho.snackball.study.StudyQueryRepository;
import me.cho.snackball.study.StudyService;
import me.cho.snackball.study.domain.Study;
import me.cho.snackball.study.dto.SearchConditions;
import me.cho.snackball.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final StudyService studyService;
    private final StudyMemberRepository studyMemberRepository;

    @GetMapping("/")
    public String homeTest(@ModelAttribute SearchConditions searchConditions,
                       @RequestParam(value = "offset", defaultValue = "0") int offset,
                       @RequestParam(value = "limit", defaultValue = "20") int limit,
                       @CurrentUser User user, Model model) {
        if (user != null) {
            String profileImage = user.getProfileImage();
            model.addAttribute("profileImage", profileImage);
        }

        System.out.println("검색 조건: " + searchConditions.getKeyword() + ", " + searchConditions.getTag() + ", " + searchConditions.getLocation());

        List<Study> studies = studyService.findByPublished(searchConditions, offset, limit);
        model.addAttribute("studies", studies);

        // 전체 항목 수 조회
        long totalElements = studyService.countByPublished(searchConditions);
        // 페이지 번호 계산
        int currentPage = (offset / limit) + 1;  // offset을 기준으로 현재 페이지 계산 (1부터 시작)
        // 전체 페이지 수 계산
        int totalPages = (int) Math.ceil((double) totalElements / limit);

        model.addAttribute("currentPage", currentPage);  // 현재 페이지
        model.addAttribute("totalPages", totalPages);  // 전체 페이지 수
        model.addAttribute("totalElements", totalElements);  // 전체 데이터 개수
        model.addAttribute("limit", limit);  // limit 값

        model.addAttribute("banner", "https://snackball-static-files.s3.ap-northeast-2.amazonaws.com/banner.png");

        return "index";
    }

    @GetMapping("/test")
    public String home(@ModelAttribute SearchConditions searchConditions,
                       @RequestParam(value = "page", defaultValue = "1") int page,
                       @RequestParam(value = "limit", defaultValue = "20") int limit,
                       @CurrentUser User user, Model model) {
        if (user != null) {
            String profileImage = user.getProfileImage();
            model.addAttribute("profileImage", profileImage);
        }

        System.out.println("검색 조건: " + searchConditions.getKeyword() + ", " + searchConditions.getTag() + ", " + searchConditions.getLocation());

        Page<Study> studies = studyService.findByPublishedPage(searchConditions, page - 1, limit);
        model.addAttribute("studies", studies);
        model.addAttribute("currentPage", page);
        model.addAttribute("limit", limit);
        model.addAttribute("totalPages", studies.getTotalPages());
        model.addAttribute("totalElements", studies.getTotalElements());
        model.addAttribute("searchConditions", searchConditions);

        model.addAttribute("banner", "https://snackball-static-files.s3.ap-northeast-2.amazonaws.com/banner.png");

        return "index";
    }


}
