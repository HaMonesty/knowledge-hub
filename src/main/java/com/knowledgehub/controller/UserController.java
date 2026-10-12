package com.knowledgehub.controller;

import com.knowledgehub.vo.UserVO;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @GetMapping("/me")
    public UserVO me(
            @AuthenticationPrincipal UserVO currentUser) {

        return currentUser;
    }
}