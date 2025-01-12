package me.cho.snackball.settings;

import lombok.RequiredArgsConstructor;
import me.cho.snackball.domain.User;
import me.cho.snackball.repository.UserRepository;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class NicknameValidator implements Validator {

    private final UserRepository userRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return UpdateProfileForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        UpdateProfileForm form = (UpdateProfileForm) target;

        User user = userRepository.findByNickname(form.getNickname()).orElse(null);

        if (user != null) {
            errors.rejectValue("nickname", null, "이미 사용중인 닉네임입니다.");
        }
    }
}
