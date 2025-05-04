package com.benchpress200.springsecuritytutorial.auth;

import com.benchpress200.springsecuritytutorial.domain.User;
import com.benchpress200.springsecuritytutorial.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    private final TokenManager tokenManager;
    private final UserRepository userRepository;


    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String accessToken = findAccessToken(request);

        if (accessToken == null) {
            System.out.println("token is null");
            filterChain.doFilter(request, response);

            return;
        }

        if (tokenManager.isExpired(accessToken)) {
            System.out.println("token expired");
            filterChain.doFilter(request, response);

            return;
        }

        long userId = tokenManager.getUserId(accessToken);

        // 해당 유저 조회
        User user = userRepository.findById(userId);
        if (user == null) {
            filterChain.doFilter(request, response);
            return;
        }

        CustomUserDetails customUserDetails = new CustomUserDetails(user);
        //스프링 시큐리티 인증 토큰 생성
        Authentication authentication = new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication); // 해당 요청 담당 쓰레드가 소유

        filterChain.doFilter(request, response);
    }

    public String findAccessToken(final HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("Authorization")) {
                return cookie.getValue();
            }
        }

        return null;
    }
}
