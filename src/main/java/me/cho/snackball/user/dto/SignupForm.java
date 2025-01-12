package me.cho.snackball.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class SignupForm {

    @Email
    @NotBlank
    private String username;

    @NotBlank
    @Length(min = 3, max = 10)
    @Pattern(regexp = "^[ㄱ-ㅎ가-힣a-zA-Z0-9]{3,10}$")
    private String nickname;

    @NotBlank
    @Length(min = 8, max = 20)
    private String password;
}
