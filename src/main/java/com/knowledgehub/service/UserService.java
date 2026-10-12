package com.knowledgehub.service;

import com.knowledgehub.dto.UserLoginDTO;
import com.knowledgehub.dto.UserRegisterDTO;
import com.knowledgehub.entity.User;
import com.knowledgehub.vo.LoginVO;
import com.knowledgehub.vo.UserVO;

public interface UserService {

    UserVO register(UserRegisterDTO dto);
    LoginVO login(UserLoginDTO dto);
    UserVO findActiveUserById(Long userId);
}