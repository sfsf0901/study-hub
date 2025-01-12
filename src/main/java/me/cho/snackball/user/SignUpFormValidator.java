package me.cho.snackball.user;

import lombok.RequiredArgsConstructor;
import me.cho.snackball.repository.UserRepository;
import me.cho.snackball.user.dto.SignupForm;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class SignUpFormValidator implements Validator {

    private final UserRepository userRepository;


    @Override
    public boolean supports(Class<?> clazz) {
        return SignupForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        SignupForm signUpForm = (SignupForm) target;

        if (userRepository.existsByUsername(signUpForm.getUsername())) {
            errors.rejectValue("email", "invalid.email", new Object[]{signUpForm.getUsername()}, "이미 사용중인 이메일입니다.");
        }
        if (userRepository.existsByNickname(signUpForm.getNickname())) {
            errors.rejectValue("nickname", "invalid.nickname", new Object[]{signUpForm.getNickname()}, "이미 사용중인 닉네임입니다.");
        }
    }
}
