package me.cho.snackball.user;

import jakarta.annotation.PostConstruct;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.cho.snackball.config.AppProperties;
import me.cho.snackball.global.s3.S3Service;
import me.cho.snackball.settings.location.*;
import me.cho.snackball.settings.location.domain.Location;
import me.cho.snackball.settings.location.domain.UserLocation;
import me.cho.snackball.settings.studyTag.StudyTagService;
import me.cho.snackball.settings.studyTag.domain.StudyTag;
import me.cho.snackball.study.domain.StudyStudyTag;
import me.cho.snackball.user.domain.User;
import me.cho.snackball.settings.studyTag.domain.UserStudyTag;
import me.cho.snackball.global.security.CustomUserDetails;
import me.cho.snackball.settings.notification.UpdateNotificationsForm;
import me.cho.snackball.settings.password.UpdatePasswordForm;
import me.cho.snackball.settings.profile.UpdateProfileForm;
import me.cho.snackball.settings.studyTag.UpdateStudyTagsForm;
import me.cho.snackball.settings.studyTag.StudyTagRepository;
import me.cho.snackball.settings.studyTag.UserStudyTagRepository;
import me.cho.snackball.user.dto.SignupForm;
import org.modelmapper.ModelMapper;
import org.springframework.context.MessageSourceAware;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final StudyTagService studyTagService;
    private final UserStudyTagRepository userStudyTagRepository;
    private final LocationRepository locationRepository;
    private final UserLocationRepository userLocationRepository;
    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final AppProperties appProperties;
    private final S3Service s3Service;

    @PostConstruct
    public void initLocationData() throws IOException {
        if (locationRepository.count() == 0) {
            Resource resource = new ClassPathResource("location_kr.csv");

            try (InputStream inputStream = resource.getInputStream();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                List<Location> locations = reader.lines()
                        .map(line -> {
                            String[] parts = line.split(",");
                            return Location.builder().province(parts[0]).city(parts[1]).build();
                        }).toList();
                locationRepository.saveAll(locations);
            }
        }
    }


/*    public User processNewUser(SignupForm signUpForm) {
        User newUser = saveUser(signUpForm);
        sendSignUpConfirmEmail(newUser);
        return newUser;
    }*/

    public User saveUser(SignupForm signUpForm) {
        String imgUrl = "https://snackball-static-files.s3.ap-northeast-2.amazonaws.com/54eec2d7-6382-44bb-a8f3-5cc28ae1ecb6_AnonymousProfileImg.png";
        User user = User.createUser(signUpForm, passwordEncoder.encode(signUpForm.getPassword()), imgUrl);
        return userRepository.save(user);
    }

    public void sendSignUpConfirmEmail(User user) {
        user.genToken();

        Context context = new Context();
        context.setVariable("nickname", user.getNickname());
        context.setVariable("message1", "SNACKBALL의 회원이 되셨습니다. 회원님의 ID는 " + user.getUsername() +"입니다.");
        context.setVariable("message2", "이메일 주소를 인증하려면 링크 클릭:");
        context.setVariable("host", appProperties.getHost());
        context.setVariable("link", "/checkemailtoken?token=" + user.getEmailCheckToken() + "&email=" + user.getUsername());
        String htmlMessage = templateEngine.process("mail/simpleLink", context);


        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
            mimeMessageHelper.setTo(user.getUsername());
            mimeMessageHelper.setSubject("SNACKBALL 이메일 인증");
            mimeMessageHelper.setText(htmlMessage, true);
            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            log.error("#####인증 이메일 전송 실패", e);
        }
    }

    public void completeSignUp(User user, HttpServletRequest request) {
        user.completeSignUp();
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


    public void sendLoginLink(User user) {
        user.genToken();

        Context context = new Context();
        context.setVariable("nickname", user.getNickname());
        context.setVariable("message1", "SNACKBALL의 ID " + user.getUsername() + "로 로그인 링크를 요청하셨습니다.");
        context.setVariable("message2", "로그인하려면 링크 클릭:");
        context.setVariable("host", appProperties.getHost());
        context.setVariable("link", "/loginbyemail?token=" + user.getEmailCheckToken() + "&email=" + user.getUsername());
        String htmlMessage = templateEngine.process("mail/simpleLink", context);

        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
            mimeMessageHelper.setTo(user.getUsername());
            mimeMessageHelper.setSubject("SNACKBALL 로그인");
            mimeMessageHelper.setText(htmlMessage, true);
            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            log.error("#####인증 이메일 전송 실패", e);
        }
    }

    public void updateProfile(User user, UpdateProfileForm updateProfileForm, HttpServletRequest request) {
        User findUser = userRepository.findByUsername(user.getUsername()).orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 회원입니다: " + user.getUsername()));

        findUser.setNickname(updateProfileForm.getNickname());
        findUser.setOccupation(updateProfileForm.getOccupation());
        findUser.setCompany(updateProfileForm.getCompany());
        findUser.setUrl(updateProfileForm.getUrl());
        findUser.setDescription(updateProfileForm.getDescription());

        if (updateProfileForm.getImageFile() != null) {
            String imageUrl = s3Service.uploadImage(updateProfileForm.getImageFile(), findUser);
            findUser.setProfileImage(imageUrl);
            login(findUser, request);
        }
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
        Set<UserStudyTag> userStudyTags = findUser.getUserStudyTags();

        StudyTag studyTag = studyTagService.createOrFindStudyTag(updateStudyTagsForm.getTagName());

        boolean exists = userStudyTags.stream()
                .anyMatch(userStudyTag -> userStudyTag.getStudyTag().getId().equals(studyTag.getId()));
        if (!exists) {
            //TODO cascade = CascadeType.ALL 때문에 save 안해 됨
            UserStudyTag.createUserStudyTag(findUser, studyTag);
        }

//        boolean duplicate = false;
//        for (UserStudyTag userStudyTag : findUser.getUserStudyTags()) {
//            if (userStudyTag.getStudyTag().getId().equals(studyTag.getId())) {
//                duplicate = true;
//            }
//        }
//        if (!duplicate) {
//            //TODO cascade = CascadeType.ALL 때문에 save 안해 됨
//            UserStudyTag.createUserStudyTag(findUser, studyTag);
//        }
    }

    public void removeUserStudyTag(User user, UserStudyTag userStudyTag) {
        User findUser = userRepository.findByUsername(user.getUsername()).orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 회원입니다: " + user.getUsername()));

        findUser.getUserStudyTags().remove(userStudyTag);
        userStudyTagRepository.delete(userStudyTag);
    }

    public Set<UserStudyTag> getUserStudyTags(User user) {
        User findUser = userRepository.findById(user.getId()).orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 회원입니다: " + user.getUsername()));
        return findUser.getUserStudyTags();
    }

    public void addLocation(User user, Location location) {
        User findUser = userRepository.findByUsername(user.getUsername()).orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 회원입니다: " + user.getUsername()));

        Set<UserLocation> userLocations = findUser.getUserLocations();

//        boolean duplicate = false;
//        for (UserLocation userLocation : findUser.getUserLocations()) {
//            if (userLocation.getId().equals(location.getId())) {
//                duplicate = true;
//            }
//        }
//
        boolean exists = userLocations.stream()
                .anyMatch(userLocation -> userLocation.getLocation().getId().equals(location.getId()));
        if (!exists) {
            UserLocation.createUserLocation(findUser, location);
        }
    }

    public void removeUserLocation(User user, UserLocation userLocation) {
        User findUser = userRepository.findByUsername(user.getUsername()).orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 회원입니다: " + user.getUsername()));

        findUser.getUserLocations().remove(userLocation);
        userLocationRepository.delete(userLocation);
    }

    public Set<UserLocation> getUserLocations(User user) {
        User findUser = userRepository.findById(user.getId()).orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 회원입니다: " + user.getUsername()));
        return findUser.getUserLocations();
    }
}
