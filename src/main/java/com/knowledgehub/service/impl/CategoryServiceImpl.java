package com.knowledgehub.service.impl;


import com.knowledgehub.dto.CategoryCreateDTO;
import com.knowledgehub.entity.Category;
import com.knowledgehub.exception.BusinessException;
import com.knowledgehub.mapper.CategoryMapper;
import com.knowledgehub.service.CategoryService;
import com.knowledgehub.vo.CategoryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private  final CategoryMapper categoryMapper;

    /**
     * 规范化分类名称，检查重复并保存分类。
     *
     * @param categoryCreateDTO 分类创建参数
     * @return 分类ID和名称
     */
    @Override
    @Transactional
    public CategoryVO create(CategoryCreateDTO categoryCreateDTO){
        String name = categoryCreateDTO.getName().strip();

        if(name.isBlank() || name.length() > 32){
            throw new BusinessException(
                    "INVALID_CATEGORRY_NAME",
                    "分类名称不能为空且不能超过32个字符"
            );
        }
        // 提前检查用于提供明确提示，最终唯一性仍由数据库保证。
        if (categoryMapper.countByName(name) > 0) {
            throw categoryAlreadyExists();
        }
        Category category = new Category();
        category.setName(name);

        try {
            categoryMapper.insert(category);
        } catch (DuplicateKeyException ex) {
            // 处理并发请求同时通过提前检查的情况。
            throw categoryAlreadyExists();
        }

        CategoryVO vo = new CategoryVO();
        vo.setId(category.getId());
        vo.setName(category.getName());
        return vo;

    }
    /**
     * 构造分类名称重复的业务异常。
     *
     * @return 分类重复异常
     */
    private BusinessException categoryAlreadyExists() {
        return new BusinessException(
                "CATEGORY_ALREADY_EXISTS",
                "分类名称已存在"
        );
    }




}
