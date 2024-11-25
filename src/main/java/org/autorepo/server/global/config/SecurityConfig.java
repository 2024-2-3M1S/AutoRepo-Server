package org.autorepo.server.global.config;

import lombok.RequiredArgsConstructor;
import org.autorepo.server.domain.user.service.CustomOAuth2UserService;
import org.autorepo.server.domain.user.service.OAuth2FailureHandler;
import org.autorepo.server.domain.user.service.OAuth2SuccessHandler;
import org.autorepo.server.global.common.jwt.JwtAuthenticationFilter;
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

    private static final String[] WHITE_LIST = {"/favicon.ico", "/api/user", "/api/token/refresh"};

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/api/template/**").permitAll() // 토큰 없이 임시 허용
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers(WHITE_LIST).permitAll()
                        .anyRequest().permitAll()
                )
                .oauth2Login(oauth2 -> {
                    oauth2
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