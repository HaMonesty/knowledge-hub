package com.knowledgehub.controller;


import com.knowledgehub.dto.CategoryCreateDTO;
import com.knowledgehub.service.CategoryService;
import com.knowledgehub.vo.CategoryVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * 创建分类，访问权限由SecurityConfig 限制为管理员
     * @param categoryCreateDTO
     * @return 新分类的ID和名称
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryVO create(@Valid @RequestBody CategoryCreateDTO categoryCreateDTO){
        return categoryService.create(categoryCreateDTO);

    }
}
