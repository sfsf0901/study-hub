package me.cho.snackball.global.security.config;

import lombok.RequiredArgsConstructor;
import me.cho.snackball.global.security.CustomUserDetailsService;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import javax.sql.DataSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final DataSource dataSource;

    @Bean
    public PersistentTokenRepository tokenRepository() {
        JdbcTokenRepositoryImpl tokenRepository = new JdbcTokenRepositoryImpl();
        tokenRepository.setDataSource(dataSource);
        return tokenRepository;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        //인가 설정
        http.authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/signup", "/login", "/",
                                "/checkemailtoken",
                                "/emaillogin", "/checkemaillogin", "/loginlink", "/loginbyemail"
//                                "/node_modules/**"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET, "/profile/**").permitAll()
                        .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                        .anyRequest().authenticated()
        );

        http.csrf(AbstractHttpConfigurer::disable);

        http.formLogin(form -> form
                .loginPage("/login")
                .permitAll());

        http.logout(logout -> logout
                .logoutSuccessUrl("/")
                .permitAll());

        http.rememberMe(rememberMe -> rememberMe
                .userDetailsService(userDetailsService)
                .tokenRepository(tokenRepository()));

        return http.build();
    }
}
