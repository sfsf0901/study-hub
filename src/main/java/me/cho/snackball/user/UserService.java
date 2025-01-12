package me.cho.snackball.user;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.cho.snackball.domain.Authority;
import me.cho.snackball.domain.User;
import me.cho.snackball.repository.UserRepository;
import me.cho.snackball.global.security.CustomUserDetails;
import me.cho.snackball.settings.UpdateNotificationsForm;
import me.cho.snackball.settings.UpdatePasswordForm;
import me.cho.snackball.settings.UpdateProfileForm;
import me.cho.snackball.user.dto.SignupForm;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final JavaMailSender javaMailSender;
    private final PasswordEncoder passwordEncoder;


    public User processNewAccount(SignupForm signUpForm) {
        User newUser = saveUser(signUpForm);
        newUser.generateEmailCheckToken();
        sendSignUpConfirmEmail(newUser);
        return newUser;
    }

    private User saveUser(SignupForm signUpForm) {
        User user = User.builder()
                .username(signUpForm.getUsername())
                .nickname(signUpForm.getNickname())
                .password(passwordEncoder.encode(signUpForm.getPassword()))
                .authority(Authority.ROLE_USER)
                .studyCreatedByWeb(true)
                .studyEnrollmentResultByWeb(true)
                .studyUpdatedByWeb(true)
                .build();
        return userRepository.save(user);
    }

    private void sendSignUpConfirmEmail(User newUser) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(newUser.getUsername());
        mailMessage.setSubject("스낵볼, 회원 가입 인증");
        mailMessage.setText("/check-email-token?token=" + newUser.getEmailCheckToken() + "&email=" + newUser.getUsername());
        javaMailSender.send(mailMessage);
    }

    public void completeSignUp(User user, HttpServletRequest request) {
        user.completeSignUp(); //TODO 반영 되나?? 안될 거 같은데
        login(user, request);
    }

    public void login(User user, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                        new CustomUserDetails(user),
                        user.getPassword(),
                        List.of(new SimpleGrantedAuthority(user.getAuthority().toString())) // 권한 추가 가능
                );

        SecurityContextHolder.getContext().setAuthentication(token);

        // 세션에 SecurityContext 저장
        HttpSession session = request.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());
    }

    public void updateProfile(User user, UpdateProfileForm form) {
        user.setNickname(form.getNickname());
        user.setDescription(form.getDescription());
        user.setOccupation(form.getOccupation());
        user.setCompany(form.getCompany());
        user.setUrl(form.getUrl());
        userRepository.save(user);
    }

    public void updatePassword(User user, UpdatePasswordForm updatePasswordForm) {
        user.setPassword(passwordEncoder.encode(updatePasswordForm.getPassword()));
        userRepository.save(user);
    }

    public void updateNotifications(User user, UpdateNotificationsForm updateNotificationsForm) {
        user.setStudyCreatedByEmail(updateNotificationsForm.isStudyCreatedByEmail());
        user.setStudyCreatedByWeb(updateNotificationsForm.isStudyCreatedByWeb());
        user.setStudyUpdatedByEmail(updateNotificationsForm.isStudyUpdatedByEmail());
        user.setStudyUpdatedByWeb(updateNotificationsForm.isStudyUpdatedByWeb());
        user.setStudyEnrollmentResultByEmail(updateNotificationsForm.isStudyEnrollmentResultByEmail());
        user.setStudyEnrollmentResultByWeb(updateNotificationsForm.isStudyEnrollmentResultByWeb());
        userRepository.save(user);
    }
}
