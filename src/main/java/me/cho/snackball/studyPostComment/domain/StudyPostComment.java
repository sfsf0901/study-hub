package me.cho.snackball.studyPostComment.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import me.cho.snackball.global.BaseUserEntity;
import me.cho.snackball.studyPost.domain.StudyPost;
import me.cho.snackball.studyPostComment.dto.CreateStudyPostCommentForm;
import me.cho.snackball.user.domain.User;

@Entity
@Getter @Setter @EqualsAndHashCode (of = "id")
@AllArgsConstructor @NoArgsConstructor
public class StudyPostComment extends BaseUserEntity {

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
    private StudyPost studyPost;


    public static StudyPostComment create(StudyPost studyPost, User user, CreateStudyPostCommentForm form) {
        StudyPostComment studyComment = new StudyPostComment();
        studyComment.setContent(form.getContent());
        studyComment.setUser(user);
        studyComment.setStudyPost(studyPost);
        return studyComment;
    }

}
