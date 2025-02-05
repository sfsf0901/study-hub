package me.cho.snackball.study.board.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.cho.snackball.global.BaseUserEntity;
import org.hibernate.validator.constraints.Length;

@Data
@AllArgsConstructor @NoArgsConstructor
public class CreateStudyPostForm extends BaseUserEntity {

    @NotBlank
    @Length(max = 30)
    private String title;

    @NotBlank
    private String content;
}
