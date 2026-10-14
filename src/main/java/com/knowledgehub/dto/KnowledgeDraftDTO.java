package com.knowledgehub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class KnowledgeDraftDTO {

    @NotBlank(message = "标题不能为空")
    @Size(max = 200, message = "标题不能超过200个字符")
    private String title;

    @Size(max = 500, message = "摘要不能超过500个字符")
    private String summary;

    @Size(max = 100000, message = "正文不能超过100000个字符")
    private String content;

    @Positive(message = "分类ID必须为正数")
    private Long categoryId;
}