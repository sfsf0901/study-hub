package me.cho.snackball;

import lombok.RequiredArgsConstructor;
import me.cho.snackball.global.security.CustomUserDetailsService;
import me.cho.snackball.user.UserService;
import me.cho.snackball.user.dto.SignupForm;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

@RequiredArgsConstructor
public class WithUserSecurityContextFactory implements WithSecurityContextFactory<WithUser> {

    private final CustomUserDetailsService userDetailsService;
    private final UserService userService;

    @Override
    public SecurityContext createSecurityContext(WithUser withUser) {
        String username = withUser.value();

        SignupForm signupForm = new SignupForm();
        signupForm.setUsername(username);
        signupForm.setNickname("testuser");
        signupForm.setPassword("12345678");
        userService.processNewAccount(signupForm);

        UserDetails principle = userDetailsService.loadUserByUsername(username);
        Authentication authentication = new UsernamePasswordAuthenticationToken(principle, principle.getPassword(), principle.getAuthorities());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        return context;
    }
}
