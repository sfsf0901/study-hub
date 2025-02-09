package me.cho.snackball.study.board.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.cho.snackball.global.BaseUserEntity;
import me.cho.snackball.study.board.domain.StudyPost;
import org.hibernate.validator.constraints.Length;

@Data
@AllArgsConstructor @NoArgsConstructor
public class UpdateStudyPostForm extends BaseUserEntity {

    private Long studyPostId;

    @NotBlank
    @Length(max = 30)
    private String title;

    @NotBlank
    private String content;

    public UpdateStudyPostForm(StudyPost studyPost) {
        this.studyPostId = studyPost.getId();
        this.title = studyPost.getTitle();
        this.content = studyPost.getContent();
    }
}
