package me.cho.snackball.notification;

import me.cho.snackball.notification.domain.Notification;
import me.cho.snackball.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    long countByUserAndChecked(User user, boolean checked);

    @Transactional
    List<Notification> findByUserAndCheckedOrderByCreatedDateDesc(User user, boolean checked);

    @Transactional
    void deleteByUserAndChecked(User user, boolean checked);

    long countByUser(User user);

    List<Notification> findByUserOrderByCreatedDateDesc(User user);
}
