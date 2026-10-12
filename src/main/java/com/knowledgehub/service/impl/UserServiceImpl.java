package com.knowledgehub.service.impl;


import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.knowledgehub.dto.UserLoginDTO;
import com.knowledgehub.dto.UserRegisterDTO;
import com.knowledgehub.entity.User;
import com.knowledgehub.exception.BusinessException;
import com.knowledgehub.mapper.UserMapper;
import com.knowledgehub.security.JwtService;
import com.knowledgehub.service.UserService;
import com.knowledgehub.vo.LoginVO;
import com.knowledgehub.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Wrapper;
import java.util.Locale;
import com.knowledgehub.dto.UserLoginDTO;
import com.knowledgehub.security.JwtService;
import com.knowledgehub.vo.LoginVO;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    @Transactional
    public UserVO register(UserRegisterDTO dto){
        //1.规范化用户名
        String username = dto.getUsername().toLowerCase(Locale.ROOT);

        //2.提前检查重复，提供明确的业务提示
//        Long count = userMapper.selectCount( //这是用mybatis-plus的方法
//                Wrappers.<User>lambdaQuery()
//                        .eq(User::getUsername, username)
//
//        );
        Long count = userMapper.countByUsername(username);
        if (count > 0){
            throw usernameAlreadyExists();

        }

        //3.构建数据库实体
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        user.setNickname(
                dto.getNickname() == null ? username : dto.getNickname()
        );
        user.setRole("USER");
        user.setStatus("ACTIVE");

        //4.写入数据库，处理并发注册导致的唯一键冲突
        try{
            userMapper.insert(user);
        } catch (DuplicateKeyException ex){
            throw usernameAlreadyExists();
        }

        //5.返回允许暴露的字段
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setRole(user.getRole());

        return vo;

    }

    /**用户登录
     *
     * @param dto
     * @return
     */
    @Override
    public LoginVO login(UserLoginDTO dto) {
        //1.规范化用户名，密码保持原样
        String username = dto.getUsername().toLowerCase(Locale.ROOT);

        //2.查询用户
        User user = userMapper.selectByUsername(username);
        // 3. 检查用户是否存在，以及账号是否可用
        if (user == null || !"ACTIVE".equals(user.getStatus())) {
            throw authenticationFailed();
        }

        //4.验证密码
        boolean passwordMatches = passwordEncoder.matches(
               dto.getPassword(),
               user.getPasswordHash()
        );
        if(!passwordMatches){
            throw authenticationFailed();
        }

        //5.签发令牌并返回
        LoginVO vo = new LoginVO();
        vo.setAccessToken(jwtService.generateToken(user.getId()));
        vo.setTokenType("Bearer");
        vo.setExpiresIn(jwtService.getExpirationSeconds());

        return vo;
    }

    private BusinessException usernameAlreadyExists(){
        return new BusinessException(
                "USERNAME_ALREADY_EXISTS",
                "用户名已存在"
        );
    }
    private  BusinessException authenticationFailed(){
        return new BusinessException(
                "AUTHENTICATION_FAILED",
                "账号或密码错误"
        );
    }
    @Override
    public UserVO findActiveUserById(Long userId){
        User user = userMapper.selectById(userId);

        if(user == null || !"ACTIVE".equals(user.getStatus())){
            return null;
        }
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setRole(user.getRole());

        return vo;
    }

}
