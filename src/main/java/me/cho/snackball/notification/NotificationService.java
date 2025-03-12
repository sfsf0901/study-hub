package me.cho.snackball.notification;

import lombok.RequiredArgsConstructor;
import me.cho.snackball.notification.domain.Notification;
import me.cho.snackball.user.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public Notification findById(Long id) {
        return notificationRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("해당 댓글이 존재하지 않습니다. notificationId: " + id));
    }

    @Transactional
    public void deleteAll(List<Long> notificationIds, User user) {
        for (Long notificationId : notificationIds) {
            Notification notification = findById(notificationId);
            if (!notification.getUser().equals(user)) {
                throw new IllegalStateException("삭제 권한이 없습니다. userId: " + user.getId() + " notificationId: " + notificationId);
            }
        }

        notificationRepository.deleteAllById(notificationIds);
    }

    @Transactional
    public void markAsChecked(Long notificationId, User user) {
        Notification notification = findById(notificationId);
        if (!notification.getUser().equals(user)) {
            throw new IllegalStateException("삭제 권한이 없습니다. userId: " + user.getId() + " notificationId: " + notificationId);
        }

        notification.setChecked(true);
    }
}
