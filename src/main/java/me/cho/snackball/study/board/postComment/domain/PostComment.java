package me.cho.snackball.study.board.postComment.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import me.cho.snackball.global.BaseUserEntity;
import me.cho.snackball.study.board.domain.StudyPost;
import me.cho.snackball.study.domain.Study;
import me.cho.snackball.study.studyComment.dto.CreateStudyCommentForm;
import me.cho.snackball.user.domain.User;

@Entity
@Getter @Setter @EqualsAndHashCode (of = "id")
@AllArgsConstructor @NoArgsConstructor
public class PostComment extends BaseUserEntity {

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


    public static PostComment create(StudyPost studyPost, User user, CreateStudyCommentForm form) {
        PostComment studyComment = new PostComment();
        studyComment.setContent(form.getContent());
        studyComment.setUser(user);
        studyComment.setStudyPost(studyPost);
        return studyComment;
    }

}
