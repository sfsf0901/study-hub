package me.cho.snackball.study.studyComment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import me.cho.snackball.global.BaseUserEntity;
import org.hibernate.validator.constraints.Length;

@Data
@AllArgsConstructor @NoArgsConstructor
public class CreateStudyCommentForm extends BaseUserEntity {

    @NotBlank
    @Length(max = 250)
    private String content;
}
