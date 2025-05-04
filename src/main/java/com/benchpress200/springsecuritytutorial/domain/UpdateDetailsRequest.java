package com.benchpress200.springsecuritytutorial.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
public class UpdateDetailsRequest {
    @Setter
    private long userId;
    private String name;
}
