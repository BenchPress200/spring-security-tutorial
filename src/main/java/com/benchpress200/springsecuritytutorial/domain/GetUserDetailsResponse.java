package com.benchpress200.springsecuritytutorial.domain;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GetUserDetailsResponse {
    private long id;
    private String name;

    public static GetUserDetailsResponse from(User user) {
        return GetUserDetailsResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .build();
    }
}
