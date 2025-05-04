package com.benchpress200.springsecuritytutorial.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class User {
    private long id;
    private String name;
    private String password;
    private String role;
}
