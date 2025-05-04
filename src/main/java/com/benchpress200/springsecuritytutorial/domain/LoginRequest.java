package com.benchpress200.springsecuritytutorial.domain;

import lombok.Getter;

@Getter
public class LoginRequest {
    private String name;
    private String password;
}
