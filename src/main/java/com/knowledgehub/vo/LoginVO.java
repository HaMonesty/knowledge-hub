package com.knowledgehub.vo;

import lombok.Data;

@Data
public class LoginVO {

    private String accessToken;

    private String tokenType;

    private Long expiresIn;
}