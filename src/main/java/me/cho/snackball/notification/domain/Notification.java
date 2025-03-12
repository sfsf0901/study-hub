package me.cho.snackball.notification.domain;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import me.cho.snackball.global.BaseEntity;
import me.cho.snackball.user.domain.User;

@Entity
@Getter
@Setter
@EqualsAndHashCode(of = "id")
public class Notification extends BaseEntity {

    @Id
    @GeneratedValue
    private Long id;

    private String title;

    private String studyTitle;

    private String link;

    private String message;

    private boolean checked;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    private NotificationType notificationType;

    public static Notification create(String title, String studyTitle, String link, String message, User user, NotificationType notificationType) {
        Notification notification = new Notification();
        notification.title = title;
        notification.studyTitle = studyTitle;
        notification.link = link;
        notification.message = message;
        notification.checked = false;
        notification.user = user;
        notification.notificationType = notificationType;
        return notification;
    }

    public void markAsChecked() {
        this.checked = true;
    }
}
