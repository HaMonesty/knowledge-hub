package com.knowledgehub.service;

import com.knowledgehub.dto.CategoryCreateDTO;
import com.knowledgehub.vo.CategoryVO;

public interface CategoryService {

    /**
     * 创建分类。
     *
     * @param dto 分类创建参数
     * @return 创建后的分类信息
     */
    CategoryVO create(CategoryCreateDTO dto);
}