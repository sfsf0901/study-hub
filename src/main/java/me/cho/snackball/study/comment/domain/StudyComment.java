package me.cho.snackball.study.comment.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import me.cho.snackball.global.BaseUserEntity;
import me.cho.snackball.study.comment.dto.CreateStudyCommentForm;
import me.cho.snackball.study.domain.Study;
import me.cho.snackball.study.dto.CreateStudyForm;
import me.cho.snackball.user.domain.User;

@Entity
@Getter @Setter @EqualsAndHashCode (of = "id")
@AllArgsConstructor @NoArgsConstructor
public class StudyComment extends BaseUserEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String content;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_id")
    private Study study;


    public static StudyComment create(Study study, User user, CreateStudyCommentForm form) {
        StudyComment studyComment = new StudyComment();
        studyComment.setContent(form.getContent());
        studyComment.setUser(user);
        studyComment.setStudy(study);
        return studyComment;
    }

}
