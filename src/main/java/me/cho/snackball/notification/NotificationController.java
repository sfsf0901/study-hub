package me.cho.snackball.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.cho.snackball.global.security.CurrentUser;
import me.cho.snackball.notification.domain.Notification;
import me.cho.snackball.user.domain.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;

    @GetMapping("/notifications/new")
    public String getNewNotifications(@CurrentUser User user, Model model) {
        List<Notification> notifications = notificationRepository.findByUserAndCheckedOrderByCreatedDateDesc(user, false);
        long totalCount = notificationRepository.countByUser(user);
        long numberOfNotChecked = notificationRepository.countByUserAndChecked(user, false);
        model.addAttribute("notifications", notifications);
        model.addAttribute("numberOfNotChecked", numberOfNotChecked);
        model.addAttribute("totalCount", totalCount);

        return "notifications/newNotifications";
    }

    @GetMapping("/notifications")
    public String getNotifications(@CurrentUser User user, Model model) {
        List<Notification> notifications = notificationRepository.findByUserOrderByCreatedDateDesc(user);
        long totalCount = notificationRepository.countByUser(user);
        long numberOfNotChecked = notificationRepository.countByUserAndChecked(user, false);
        model.addAttribute("notifications", notifications);
        model.addAttribute("numberOfNotChecked", numberOfNotChecked);
        model.addAttribute("totalCount", totalCount);


        return "notifications/notifications";
    }

    @PostMapping("/notifications/new/delete")
    public String deleteNewNotification(@RequestParam("notificationIds") List<Long> notificationIds,
                                     @CurrentUser User user,
                                     Model model) {
        if (notificationIds == null || notificationIds.isEmpty()) {
            model.addAttribute("errorMessage", "삭제할 알림을 선택해주세요.");
        }

        notificationService.deleteAll(notificationIds, user);

        return "redirect:/notifications/new";  // 삭제 후 알림 목록 페이지로 이동
    }

    @PostMapping("/notifications/delete")
    public String deleteNotification(@RequestParam("notificationIds") List<Long> notificationIds,
                                     @CurrentUser User user,
                                     Model model) {
        if (notificationIds == null || notificationIds.isEmpty()) {
            model.addAttribute("errorMessage", "삭제할 알림을 선택해주세요.");
        }

        notificationService.deleteAll(notificationIds, user);

        return "redirect:/notifications";
    }

    @PostMapping("/notification/new/check")
    public String markAsCheckedRedirectNew(@RequestParam("notificationId") Long notificationId,
                                                @CurrentUser User user) {
        notificationService.markAsChecked(notificationId, user);

        return "redirect:/notifications/new";
    }

    @PostMapping("/notification/check")
    public String markAsChecked(@RequestParam("notificationId") Long notificationId,
                                                @CurrentUser User user) {
        notificationService.markAsChecked(notificationId, user);

        return "redirect:/notifications";
    }


}
