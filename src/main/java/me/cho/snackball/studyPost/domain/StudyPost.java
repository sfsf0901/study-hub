package me.cho.snackball.studyPost.domain;

import jakarta.persistence.*;
import lombok.*;
import me.cho.snackball.global.BaseUserEntity;
import me.cho.snackball.studyPost.dto.CreateStudyPostForm;
import me.cho.snackball.studyPost.dto.UpdateStudyPostForm;
import me.cho.snackball.study.domain.Study;
import me.cho.snackball.user.domain.User;

@Entity
@Getter @Setter @EqualsAndHashCode (of = "id")
@AllArgsConstructor @NoArgsConstructor
public class StudyPost extends BaseUserEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_id")
    private Study study;

    private String title;

    @Lob @Basic(fetch = FetchType.EAGER)
    private String content;

    private boolean isDeleted;

    public static StudyPost create(User user, Study study, CreateStudyPostForm createStudyPostForm) {
        StudyPost studyPost = new StudyPost();
        studyPost.user = user;
        studyPost.study = study;
        studyPost.title = createStudyPostForm.getTitle();
        studyPost.content = createStudyPostForm.getContent();
        return studyPost;
    }

    public void update(UpdateStudyPostForm updateStudyPostForm) {
        this.title = updateStudyPostForm.getTitle();
        this.content = updateStudyPostForm.getContent();
    }
}
