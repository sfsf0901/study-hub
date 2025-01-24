package me.cho.snackball.study.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import me.cho.snackball.user.domain.User;

import java.time.LocalDateTime;

@Entity
@Getter @Setter @EqualsAndHashCode(of = "id")
@AllArgsConstructor @NoArgsConstructor
public class StudyMember {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_id")
    private Study study;

    private boolean active;

    private LocalDateTime requestDate;

    private LocalDateTime activeDate;


    public static StudyMember create(User user, Study study) {
        StudyMember studyMember = new StudyMember();
        studyMember.setUser(user);
        studyMember.setStudy(study);
        studyMember.setActive(false);
        studyMember.setRequestDate(LocalDateTime.now());
        study.getMembers().add(studyMember);
        return studyMember;
    }
}
