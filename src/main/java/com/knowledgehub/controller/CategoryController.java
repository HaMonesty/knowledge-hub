package com.knowledgehub.controller;


import com.knowledgehub.dto.CategoryCreateDTO;
import com.knowledgehub.dto.CategoryUpdateDTO;
import com.knowledgehub.service.CategoryService;
import com.knowledgehub.vo.CategoryVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

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
    /**
     * 查询分类列表，所有登录用户均可访问。
     *
     * @return 分类信息列表
     */
    @GetMapping
    public List<CategoryVO> list() {
        return categoryService.list();
    }
    /**
     * 修改分类名称。
     *
     * @param id 分类ID
     * @param dto 修改参数
     * @return 修改后的分类信息
     */
    @PutMapping("/{id}")
    public CategoryVO update(
            @PathVariable("id") Long id,
            @Valid @RequestBody CategoryUpdateDTO dto) {
        return categoryService.update(id, dto);

    }
    /**
     * 删除分类，访问权限由SecurityConfig限制为管理员。
     *
     * @param id 分类ID
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") Long id) {
        categoryService.delete(id);
    }

}


