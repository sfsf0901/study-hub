package me.cho.snackball.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class LoginForm {

    @Email
    @NotBlank
    private String username;

    @NotBlank
    private String password;
}
