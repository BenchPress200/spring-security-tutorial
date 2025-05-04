package com.benchpress200.springsecuritytutorial.controller;

import com.benchpress200.apiresponse.ApiResponse;
import com.benchpress200.springsecuritytutorial.domain.GetUserDetailsResponse;
import com.benchpress200.springsecuritytutorial.domain.JoinRequest;
import com.benchpress200.springsecuritytutorial.domain.UpdateDetailsRequest;
import com.benchpress200.springsecuritytutorial.domain.WhoAmIResponse;
import com.benchpress200.springsecuritytutorial.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    /*
    * 회원가입
    * - 새 유저 메모리에 저장
     */
    @PostMapping("/api/v1/users")
    public ResponseEntity<?> join(final @RequestBody JoinRequest joinRequest) {
        userService.join(joinRequest);

        return ApiResponse.builder()
                .status(HttpStatus.CREATED)
                .message("join success")
                .build();
    }

    /*
    * 로그아웃
    * - 쿠키만료
     */
    @PostMapping("/api/v1/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        userService.logout(response);

        return ApiResponse.builder()
                .status(HttpStatus.NO_CONTENT)
                .message("logout success")
                .build();
    }

    /*
    * 유저 아이디 조회
    * - 인증필요
    * - 쿠키에 단긴 어세스 토큰 조회해서 해당 유저 아이디 응답
     */
    @GetMapping("/api/v1/me")
    public ResponseEntity<?> whoAmI(
            @CookieValue("Authorization") final String accessToken
    ) {
        WhoAmIResponse whoAmIResponse = userService.whoAmI(accessToken);

        return ApiResponse.builder()
                .status(HttpStatus.OK)
                .message("success")
                .data(whoAmIResponse)
                .build();
    }

    /*
    * 유저 정보 조회
    * - 인증필요
    * - 전달받은 유저 아이디 가지는 유저 데이터 조회
     */
    @GetMapping("/api/v1/users/{userId}")
    public ResponseEntity<?> getDetails(@PathVariable final long userId) {
        GetUserDetailsResponse getUserDetailsResponse = userService.getUserDetails(userId);

        return ApiResponse.builder()
                .status(HttpStatus.OK)
                .message("find success")
                .data(getUserDetailsResponse)
                .build();
    }

    /*
    * 유저 정보 수정
    * - 인증필요
     */
    @PatchMapping("/api/v1/users/{userId}")
    public ResponseEntity<?> updateDetails(
            @PathVariable final long userId,
            @RequestBody final UpdateDetailsRequest updateDetailsRequest
    ) {
        updateDetailsRequest.setUserId(userId);
        userService.updateDetails(updateDetailsRequest);

        return ApiResponse.builder()
                .status(HttpStatus.NO_CONTENT)
                .message("update success")
                .build();
    }

    /*
    * 회원탈퇴
    * - 인증필요
     */
    @DeleteMapping("/api/v1/users/{userId}")
    public ResponseEntity<?> withdraw(@PathVariable final long userId) {
        userService.withdraw(userId);

        return ApiResponse.builder()
                .status(HttpStatus.NO_CONTENT)
                .message("withdraw success")
                .build();
    }

    /*
    * ADMIN 유저만 호출 가능한 API
     */
    @GetMapping("/admin")
    public ResponseEntity<?> admin() {
        return ApiResponse.builder()
                .status(HttpStatus.OK)
                .message("admin ok")
                .build();
    }
}
