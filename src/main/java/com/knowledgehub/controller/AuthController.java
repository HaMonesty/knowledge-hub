package com.knowledgehub.controller;


import com.knowledgehub.dto.UserLoginDTO;
import com.knowledgehub.dto.UserRegisterDTO;
import com.knowledgehub.service.UserService;
import com.knowledgehub.vo.LoginVO;
import com.knowledgehub.vo.UserVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserVO regester(@Valid @RequestBody UserRegisterDTO dto){
        return userService.register(dto);
    }

    @PostMapping("/login")
    public LoginVO login(@Valid @RequestBody UserLoginDTO dto){
        return userService.login(dto);
    }
}
