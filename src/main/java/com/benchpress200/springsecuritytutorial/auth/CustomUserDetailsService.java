package com.benchpress200.springsecuritytutorial.auth;

import com.benchpress200.springsecuritytutorial.domain.User;
import com.benchpress200.springsecuritytutorial.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 이름 기반으로 유저 조회
        User user = userRepository.findByName(username);

        if (user != null) {
            return CustomUserDetails.from(user);
        }

        return null;
    }
}
