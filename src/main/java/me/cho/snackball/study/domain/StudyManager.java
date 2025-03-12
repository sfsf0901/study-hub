package me.cho.snackball.study.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import me.cho.snackball.user.domain.User;

@Entity
@Getter @Setter @EqualsAndHashCode(of = "id")
@AllArgsConstructor @NoArgsConstructor
public class StudyManager {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_id")
    private Study study;


    public static StudyManager createStudyManager(User user, Study study) {
        StudyManager studyManager = new StudyManager();
        studyManager.setUser(user);
        studyManager.setStudy(study);
        study.getManagers().add(studyManager);

        return studyManager;
    }
}
