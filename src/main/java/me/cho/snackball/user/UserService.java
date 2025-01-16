package me.cho.snackball.user;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import me.cho.snackball.settings.location.*;
import me.cho.snackball.settings.studyTag.StudyTag;
import me.cho.snackball.user.domain.User;
import me.cho.snackball.settings.studyTag.UserStudyTag;
import me.cho.snackball.global.security.CustomUserDetails;
import me.cho.snackball.settings.notification.UpdateNotificationsForm;
import me.cho.snackball.settings.password.UpdatePasswordForm;
import me.cho.snackball.settings.profile.UpdateProfileForm;
import me.cho.snackball.settings.studyTag.UpdateStudyTagsForm;
import me.cho.snackball.settings.studyTag.StudyTagRepository;
import me.cho.snackball.settings.studyTag.UserStudyTagRepository;
import me.cho.snackball.user.dto.SignupForm;
import org.modelmapper.ModelMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final StudyTagRepository studyTagRepository;
    private final UserStudyTagRepository userStudyTagRepository;
    private final LocationRepository locationRepository;
    private final UserLocationRepository userLocationRepository;
    private final JavaMailSender javaMailSender;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;

    @PostConstruct
    public void initLocationData() throws IOException {
        if (locationRepository.count() == 0) {
            Resource resource = new ClassPathResource("location_kr.csv");
            List<Location> locations = Files.readAllLines(resource.getFile().toPath(), StandardCharsets.UTF_8).stream()
                    .map(line -> {
                        String[] parts = line.split(",");
                        return Location.builder().province(parts[0]).city(parts[1]).build();
                    }).toList();
            locationRepository.saveAll(locations);
        }
    }

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

    public void addStudyTag(User user, UpdateStudyTagsForm updateStudyTagsForm) {
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
            //TODO save 안해도 저장되네???
        }
    }

    public Set<UserStudyTag> getUserStudyTags(User user) {
        User findUser = userRepository.findById(user.getId()).orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 회원입니다: " + user.getUsername()));
        return findUser.getUserStudyTags();
    }

    public void removeUserStudyTag(User user, UserStudyTag userStudyTag) {
        User findUser = userRepository.findByUsername(user.getUsername()).orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 회원입니다: " + user.getUsername()));

        findUser.getUserStudyTags().remove(userStudyTag);
        userStudyTagRepository.delete(userStudyTag);
    }

    public Set<UserLocation> getUserLocations(User user) {
        User findUser = userRepository.findById(user.getId()).orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 회원입니다: " + user.getUsername()));
        return findUser.getUserLocations();
    }

    public void addLocation(User user, Location location) {
        User findUser = userRepository.findByUsername(user.getUsername()).orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 회원입니다: " + user.getUsername()));

        boolean duplicate = false;
        for (UserLocation userLocation : findUser.getUserLocations()) {
            if (userLocation.getId().equals(location.getId())) {
                duplicate = true;
            }
        }
        if (!duplicate) {
            UserLocation.createUserLocation(findUser, location);
        }
    }

    public void removeUserLocation(User user, UserLocation userLocation) {
        User findUser = userRepository.findByUsername(user.getUsername()).orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 회원입니다: " + user.getUsername()));

        findUser.getUserLocations().remove(userLocation);
        userLocationRepository.delete(userLocation);
    }
}
