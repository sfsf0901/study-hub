package me.cho.snackball.user.domain;

import jakarta.persistence.*;
import lombok.*;
import me.cho.snackball.settings.location.domain.UserLocation;
import me.cho.snackball.settings.studyTag.domain.UserStudyTag;
import me.cho.snackball.global.BaseEntity;
import me.cho.snackball.user.dto.SignupForm;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "users")
@Getter @Setter @EqualsAndHashCode(callSuper = false, of = "id")
@AllArgsConstructor @NoArgsConstructor
public class User extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String nickname;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Authority authority;

    private Boolean isDeleted;

    private boolean emailVerified;

    private String emailCheckToken;

    private LocalDateTime emailCheckTokenGeneratedAt;

    private String description;

    private String url;

    private String occupation;

    private String company;

//    @Lob @Basic(fetch = FetchType.EAGER)
    private String profileImage;

    private boolean studyCreatedByEmail;

    private boolean studyCreatedByWeb;

    private boolean studyEnrollmentResultByEmail;

    private boolean studyEnrollmentResultByWeb;

    private boolean studyUpdatedByEmail;

    private boolean studyUpdatedByWeb;

//    @ManyToMany
//    private List<Tag> tags;
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<UserStudyTag> userStudyTags = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<UserLocation> userLocations = new ArrayList<>();


    public static User createUser(SignupForm signUpForm, String password, String imgUrl) {
        User user = new User();
        user.setUsername(signUpForm.getUsername());
        user.setNickname(signUpForm.getNickname());
        user.setPassword(password);
        user.setAuthority(Authority.ROLE_USER);
        user.setProfileImage(imgUrl);
        user.setStudyCreatedByWeb(true);
        user.setStudyEnrollmentResultByWeb(true);
        user.setStudyUpdatedByWeb(true);
        return user;
    }

    public void genToken() {
        this.emailCheckToken = UUID.randomUUID().toString();
    }

    public void completeSignUp() {
        this.emailVerified = true; //TODO 반영 되나??
    }

    public boolean isValidToken(String token) {
        return this.emailCheckToken.equals(token);
    }

    public boolean canSendConfirmEmail() {
        return this.emailCheckTokenGeneratedAt.isBefore(LocalDateTime.now().minusHours(1));
    }
}
