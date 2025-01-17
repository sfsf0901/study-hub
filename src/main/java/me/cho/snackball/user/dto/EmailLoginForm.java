package me.cho.snackball.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EmailLoginForm {

    @Email
    @NotBlank
    private String username;
}
