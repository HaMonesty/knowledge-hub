package com.knowledgehub.dto;

import com.knowledgehub.service.CategoryService;
import com.knowledgehub.vo.CategoryVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Data
@RequiredArgsConstructor
public class CategoryUpdateDTO {

    private final CategoryService categoryService;

    @NotBlank(message = "分类名称不能为空")
    @Size(max = 32, message = "分类名称不能超过32个字符")
    private String name;

    /**
     * 修改分类，访问权限由SecurityConfig限制为管理员。
     * @param id
     * @param dto
     * @return
     */
    @PutMapping("/{id}")
    public CategoryVO update(
            @PathVariable("id") Long id,
            @Valid @RequestBody CategoryUpdateDTO dto) {
        return categoryService.update(id, dto);
    }
}
