package me.cho.snackball.study.event;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.cho.snackball.config.AppProperties;
import me.cho.snackball.location.StudyLocationQueryRepository;
import me.cho.snackball.location.domain.Location;
import me.cho.snackball.notification.NotificationRepository;
import me.cho.snackball.notification.domain.Notification;
import me.cho.snackball.notification.domain.NotificationType;
import me.cho.snackball.study.domain.Study;
import me.cho.snackball.studyTag.StudyStudyTagQueryRepository;
import me.cho.snackball.studyTag.domain.StudyTag;
import me.cho.snackball.user.UserQueryRepository;
import me.cho.snackball.user.UserRepository;
import me.cho.snackball.user.domain.User;
import org.springframework.context.event.EventListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.List;

@Component
@Transactional
@RequiredArgsConstructor
@Async
@Slf4j
public class StudyEventListener {

    private final StudyStudyTagQueryRepository studyStudyTagQueryRepository;
    private final StudyLocationQueryRepository studyLocationQueryRepository;
    private final UserQueryRepository userQueryRepository;
    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;
    private final AppProperties appProperties;
    private final NotificationRepository notificationRepository;


    @EventListener
    public void handleStudyCreatedEvent(StudyCreateEvent studyCreateEvent) {
        List<StudyTag> studyTags = studyStudyTagQueryRepository.findStudyTagsByStudyId(studyCreateEvent.getStudy().getId());
        List<Location> locations = studyLocationQueryRepository.findLocationByStudyId(studyCreateEvent.getStudy().getId());
        List<User> users = userQueryRepository.findByStudyTagsAndLocations(studyTags, locations);

        users.forEach(user -> {
            if (user.isStudyCreatedByEmail()) {
                sendStudyCreatedEmail(studyCreateEvent, user);
            }

            if (user.isStudyCreatedByWeb()) {
                sendStudyCreatedByWeb(studyCreateEvent, user);
            }
        });
    }

    @EventListener
    public void handleEnrollmentAcceptEvent(EnrollmentAcceptEvent enrollmentAcceptEvent) {
        User user = enrollmentAcceptEvent.getUser();
        Study study = enrollmentAcceptEvent.getStudy();
        if (user.isStudyEnrollmentResultByEmail()) {
            sendEnrollmentAcceptEmail(user, study);
        }

        if (user.isStudyEnrollmentResultByWeb()) {
            sendEnrollmentAcceptNotification(study, user);
        }
    }

    private void sendStudyCreatedEmail(StudyCreateEvent studyCreateEvent, User user) {
        // TODO 이메일 보내기
        Context context = new Context();
        context.setVariable("nickname", user.getNickname());
        context.setVariable("message1", "회원님께서 등록하신 관심 키워드 및 활동 지역과 관련된 새로운 스터디가 만들어졌어요.");
        context.setVariable("message2", "스터디 페이지를 확인하려면 링크 클릭:");
        context.setVariable("host", appProperties.getHost());
        context.setVariable("link", "/study/" + studyCreateEvent.getStudy().getId());
        String htmlMessage = templateEngine.process("mail/simpleLink", context);


        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
            mimeMessageHelper.setTo(user.getUsername());
            mimeMessageHelper.setSubject("[스카] 새로운 스터디가 민들어졌어요");
            mimeMessageHelper.setText(htmlMessage, true);
            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            log.error("#####인증 이메일 전송 실패", e);
        }
    }

    private void sendStudyCreatedByWeb(StudyCreateEvent studyCreateEvent, User user) {
        // TODO 알림 보내기
        Notification notification = Notification.create(
                "새로운 스터디가 만들어졌어요",
                studyCreateEvent.getStudy().getTitle(),
                "/study/" + studyCreateEvent.getStudy().getId(),
                "회원님께서 등록하신 관심 키워드 및 활동 지역과 관련된 새로운 스터디가 만들어졌어요.<br><br>스터디 페이지를 확인하려면 아래 링크를 클릭해 주세요.",
                user,
                NotificationType.STUDY_CREATED);
        notificationRepository.save(notification);
    }

    private void sendEnrollmentAcceptEmail(User user, Study study) {
        // TODO 이메일 보내기
        Context context = new Context();
        context.setVariable("nickname", user.getNickname());
        context.setVariable("message1", "[" + study.getTitle() + "] 스터디 가입 신청이 수락되었습니다.");
        context.setVariable("message2", "스터디 페이지를 확인하려면 링크 클릭:");
        context.setVariable("host", appProperties.getHost());
        context.setVariable("link", "/study/" + study.getId());
        String htmlMessage = templateEngine.process("mail/simpleLink", context);


        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
            mimeMessageHelper.setTo(user.getUsername());
            mimeMessageHelper.setSubject("[스카] 스터디 가입 신청이 완료됐어요");
            mimeMessageHelper.setText(htmlMessage, true);
            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            log.error("#####인증 이메일 전송 실패", e);
        }
    }

    private void sendEnrollmentAcceptNotification(Study study, User user) {
        if (study == null || user == null) {
            log.error("study 또는 user가 null입니다! study={}, user={}", study, user);
            return; // study나 user가 null이면 알림을 만들지 않음
        }

        log.info("알림 생성 시작: study={}, user={}", study.getTitle(), user.getNickname());

        Notification notification = Notification.create(
                "스터디 가입 신청이 완료됐어요",
                study.getTitle(),
                "/study/" + study.getId(),
                "스터디 가입 신청이 수락되었습니다.<br><br>스터디 페이지를 확인하려면 아래 링크를 클릭해 주세요.",
                user,
                NotificationType.ENROLLMENT_ACCEPTED);

        if (notification == null) {
            log.error("Notification.create() 결과가 null입니다! 알림이 생성되지 않음.");
            return;
        }

        notificationRepository.save(notification);
        notificationRepository.flush(); // 강제로 DB 반영 (즉시 저장)

        log.info("알림 저장됨: notificationId={}", notification.getId());
    }

}
