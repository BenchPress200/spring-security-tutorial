package com.benchpress200.springsecuritytutorial.service;

import com.benchpress200.springsecuritytutorial.auth.TokenManager;
import com.benchpress200.springsecuritytutorial.domain.GetUserDetailsResponse;
import com.benchpress200.springsecuritytutorial.domain.JoinRequest;
import com.benchpress200.springsecuritytutorial.domain.UpdateDetailsRequest;
import com.benchpress200.springsecuritytutorial.domain.User;
import com.benchpress200.springsecuritytutorial.domain.WhoAmIResponse;
import com.benchpress200.springsecuritytutorial.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final TokenManager tokenManager;
    private final PasswordEncoder passwordEncoder;

    public void join(final JoinRequest joinRequest) {
        String name = joinRequest.getName();
        String password = joinRequest.getPassword();
        password = passwordEncoder.encode(password);

        // 저장
        userRepository.save(User.builder()
                .name(name)
                .password(password)
                .role("ROLE_USER")
                .build());
    }

    public void logout(final HttpServletResponse response) {
        Cookie accessTokenCookie = new Cookie("Authorization", null);
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(0);

        response.addCookie(accessTokenCookie);
    }

    public WhoAmIResponse whoAmI(String accessToken) {
        long userId = tokenManager.getUserId(accessToken);
        return new WhoAmIResponse(userId);
    }

    public GetUserDetailsResponse getUserDetails(final long userId) {
        User user = userRepository.findById(userId);
        return GetUserDetailsResponse.from(user);
    }

    public void updateDetails(final UpdateDetailsRequest updateDetailsRequest) {
        long userId = updateDetailsRequest.getUserId();
        User user = userRepository.findById(userId);

        String nameToUpdate = updateDetailsRequest.getName();
        user.setName(nameToUpdate);

        userRepository.update(user);
    }

    public void withdraw(final long userId) {
        userRepository.delete(userId);
    }
}
