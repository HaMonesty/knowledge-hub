package com.knowledgehub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRegisterDTO {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 4, max = 32, message = "用户名长度必须为4～32位")
    @Pattern(
            regexp = "^[A-Za-z0-9_]+$",
            message = "用户名只能包含英文字母、数字和下划线"
    )
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 64, message = "密码长度必须为8～64位")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*[0-9])[\\s\\S]+$",
            message = "密码必须同时包含字母和数字"
    )
    private String password;

    @Size(min = 1, max = 32, message = "昵称长度必须为1～32个字符")
    private String nickname;
}