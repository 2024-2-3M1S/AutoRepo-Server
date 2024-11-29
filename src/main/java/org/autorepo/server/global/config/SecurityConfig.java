package org.autorepo.server.global.config;

import lombok.RequiredArgsConstructor;
import org.autorepo.server.domain.user.service.CustomOAuth2UserService;
import org.autorepo.server.domain.user.service.OAuth2FailureHandler;
import org.autorepo.server.domain.user.service.OAuth2SuccessHandler;
import org.autorepo.server.global.common.auth.jwt.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final OAuth2FailureHandler oAuth2FailureHandler;
    private final CustomOAuth2UserService customOAuth2UserService;

    private static final String[] WHITE_LIST = {
            "/actuator/health",
            "/api/user/**",
            "/api/token/refresh",
            "/login/oauth2/**",
            "/oauth2/**",
            "/favicon.ico"
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/favicon.ico").permitAll()
                        .requestMatchers(WHITE_LIST).permitAll()
                        .anyRequest().authenticated()

                )
                .oauth2Login(oauth2 -> {
                    oauth2
                            .loginPage("/api/user/login") // 클라이언트에서 /login 호출 시 GitHub OAuth2로 리다이렉트
                            .successHandler(oAuth2SuccessHandler)
                            .failureHandler(oAuth2FailureHandler)
                            .userInfoEndpoint(userInfo -> userInfo
                                    .userService(customOAuth2UserService)
                            );
                })
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            throw new UnsupportedOperationException("UserDetailsService is not used in this app.");
        };
    }
}