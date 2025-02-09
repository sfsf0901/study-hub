package me.cho.snackball.studyPostComment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.cho.snackball.global.BaseUserEntity;
import org.hibernate.validator.constraints.Length;

@Data
@AllArgsConstructor @NoArgsConstructor
public class CreateStudyPostCommentForm extends BaseUserEntity {

    @NotBlank
    @Length(max = 250)
    private String content;
}
