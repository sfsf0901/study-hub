package me.cho.snackball.user;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.cho.snackball.domain.StudyTag;
import me.cho.snackball.domain.User;
import me.cho.snackball.domain.UserStudyTag;
import me.cho.snackball.repository.UserRepository;
import me.cho.snackball.global.security.CustomUserDetails;
import me.cho.snackball.settings.UpdateNotificationsForm;
import me.cho.snackball.settings.UpdatePasswordForm;
import me.cho.snackball.settings.UpdateProfileForm;
import me.cho.snackball.settings.UpdateStudyTagsForm;
import me.cho.snackball.studyTag.StudyTagRepository;
import me.cho.snackball.studyTag.UserStudyTagRepository;
import me.cho.snackball.user.dto.SignupForm;
import org.modelmapper.ModelMapper;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final StudyTagRepository studyTagRepository;
    private final UserStudyTagRepository userStudyTagRepository;
    private final JavaMailSender javaMailSender;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;


    public User processNewAccount(SignupForm signUpForm) {
        User newUser = saveUser(signUpForm);
        newUser.generateEmailCheckToken();
        sendSignUpConfirmEmail(newUser);
        return newUser;
    }

    private User saveUser(SignupForm signUpForm) {
        User user = User.createUser(signUpForm, passwordEncoder.encode(signUpForm.getPassword()));
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

    public void updateProfile(User user, UpdateProfileForm updateProfileForm) {
        modelMapper.map(updateProfileForm, user);
        userRepository.save(user);
    }

    public void updatePassword(User user, UpdatePasswordForm updatePasswordForm) {
        user.setPassword(passwordEncoder.encode(updatePasswordForm.getPassword()));
        userRepository.save(user);
    }

    public void updateNotifications(User user, UpdateNotificationsForm updateNotificationsForm) {
        modelMapper.map(updateNotificationsForm, user);
        userRepository.save(user);
    }

    public void addStudyTags(User user, UpdateStudyTagsForm updateStudyTagsForm) {
        User findUser = userRepository.findByUsername(user.getUsername()).orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 회원입니다: " + user.getUsername()));

        StudyTag studyTag = studyTagRepository.findByName(updateStudyTagsForm.getTagName()).orElse(null);

        if (studyTag == null) {
            studyTag = studyTagRepository.save(StudyTag.builder().name(updateStudyTagsForm.getTagName()).build());
        }

        boolean duplicate = false;
        for (UserStudyTag userStudyTag : findUser.getUserStudyTags()) {
            if (userStudyTag.getStudyTag().getId().equals(studyTag.getId())) {
                duplicate = true;
            }
        }
        if (!duplicate) {
            UserStudyTag.createUserStudyTag(findUser, studyTag);
        }
    }

    public Set<UserStudyTag> getUserStudyTags(User user) {
        User findUser = userRepository.findById(user.getId()).orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 회원입니다: " + user.getUsername()));
        return findUser.getUserStudyTags();
    }

    public void removeStudyTags(User user, UpdateStudyTagsForm updateStudyTagsForm) {
        User findUser = userRepository.findByUsername(user.getUsername()).orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 회원입니다: " + user.getUsername()));

        //TODO 커스텀 예외 만들기
        UserStudyTag userStudyTag = userStudyTagRepository.findByUserIdAndName(findUser.getId(), updateStudyTagsForm.getTagName()).orElseThrow(() -> new RuntimeException());
        findUser.getUserStudyTags().remove(userStudyTag);
        userStudyTagRepository.delete(userStudyTag);
    }
}
