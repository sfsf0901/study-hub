package me.cho.snackball.settings.studyTag;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import me.cho.snackball.user.domain.User;

@Entity
@Getter @Setter @EqualsAndHashCode(of = "id")
@Builder @AllArgsConstructor @NoArgsConstructor
public class UserStudyTag {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_tag_id")
    private StudyTag studyTag;

    private String name;


    //==생성 메서드==//
    public static UserStudyTag createUserStudyTag(User user, StudyTag studyTag) {
        UserStudyTag userStudyTag = new UserStudyTag();
        userStudyTag.setUser(user);
        userStudyTag.setStudyTag(studyTag);
        userStudyTag.setName(studyTag.getName());
        user.getUserStudyTags().add(userStudyTag);
        return userStudyTag;
    }

}
