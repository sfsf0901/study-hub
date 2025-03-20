package me.cho.snackball.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class SignupForm {

    @Email(message = "유효한 이메일 주소를 입력하세요.")
    @NotBlank
    private String username;

    @NotBlank
    @Length(min = 3, max = 10)
    @Pattern(regexp = "^[ㄱ-ㅎ가-힣a-zA-Z0-9]{3,10}$", message = "닉네임에 특수문자를 사용할 수 없습니다.")
    private String nickname;

    @NotBlank
    @Length(min = 8, max = 50, message = "비밀번호는 8~50자 사이여야 합니다.")
    private String password;
}
