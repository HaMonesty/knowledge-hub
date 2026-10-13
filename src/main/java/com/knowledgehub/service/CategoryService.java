package com.knowledgehub.service;

import com.knowledgehub.dto.CategoryCreateDTO;
import com.knowledgehub.dto.CategoryUpdateDTO;
import com.knowledgehub.vo.CategoryVO;

import java.util.List;

public interface CategoryService {

    /**
     * 创建分类。
     *
     * @param dto 分类创建参数
     * @return 创建后的分类信息
     */
    CategoryVO create(CategoryCreateDTO dto);

    /**
     * 获取分类列表。
     *
     * @return 分类信息列表
     */
    List<CategoryVO> list();
    /**
     * 修改分类名称。
     *
     * @param id 分类ID
     * @param dto 修改参数
     * @return 修改后的分类信息
     */
    CategoryVO update(Long id, CategoryUpdateDTO dto);
    /**
     * 删除分类。
     *
     * @param id 分类ID
     */
    void delete(Long id);
}