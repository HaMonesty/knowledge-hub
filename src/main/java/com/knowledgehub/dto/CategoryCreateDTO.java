package com.knowledgehub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CategoryCreateDTO {

    @NotBlank(message = "分类名称不能为空")
    @Size(max = 32, message = "分类名称不能超过32个字符")
    private String name;
}