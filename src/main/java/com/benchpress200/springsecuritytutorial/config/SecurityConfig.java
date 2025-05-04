package com.benchpress200.springsecuritytutorial.config;

import com.benchpress200.springsecuritytutorial.auth.JwtFilter;
import com.benchpress200.springsecuritytutorial.auth.LoginFilter;
import com.benchpress200.springsecuritytutorial.auth.TokenManager;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity(debug = true)
@RequiredArgsConstructor
public class SecurityConfig {
    private final AccessDeniedHandler accessDeniedHandler;
    private final AuthenticationEntryPoint authenticationEntryPoint;
    private final AuthenticationConfiguration authenticationConfiguration;
    private final TokenManager tokenManager;
    private final JwtFilter jwtFilter;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // 로그인 필터생성 및 엔드포인트 커스텀
        LoginFilter loginFilter = new LoginFilter(authenticationManager(authenticationConfiguration), tokenManager);
        loginFilter.setFilterProcessesUrl("/api/v1/login");


        http
                .csrf(AbstractHttpConfigurer::disable) // CSRF disable
                .formLogin(AbstractHttpConfigurer::disable) // 기본 제공 form 로그인 disable
                .httpBasic(AbstractHttpConfigurer::disable) // http basic 인증방식 disable


                .sessionManagement((session) -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))


                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers("/api/v1/users", "/api/v1/login").permitAll()
                        .requestMatchers("/admin").hasRole("ADMIN") // 실제 UserDetails를 통해 조회하는 role은 "ROLE_ADMIN"
                        .anyRequest().authenticated())


                .exceptionHandling((ex) -> ex
                        .authenticationEntryPoint(authenticationEntryPoint) // 401 예외 처리 커스텀 (안하면 401 예외인데 403 던지는 기본값 객체 사용할 때도 있음)
                        .accessDeniedHandler(accessDeniedHandler)) // 403 예외 처리 커스텀


                .addFilterBefore(jwtFilter, LoginFilter.class)
                .addFilterAt(loginFilter, UsernamePasswordAuthenticationFilter.class);


        return http.build();
    }
}
