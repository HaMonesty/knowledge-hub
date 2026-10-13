package com.knowledgehub.service.impl;


import com.knowledgehub.dto.CategoryCreateDTO;
import com.knowledgehub.dto.CategoryUpdateDTO;
import com.knowledgehub.entity.Category;
import com.knowledgehub.exception.BusinessException;
import com.knowledgehub.mapper.CategoryMapper;
import com.knowledgehub.service.CategoryService;
import com.knowledgehub.vo.CategoryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
     * 查询分类并转换为对外返回的VO
     * @return
     */
    @Override
    public List<CategoryVO> list() {
        return categoryMapper.selectAll()
                .stream()
                .map(category -> {
                    CategoryVO vo = new CategoryVO();
                    vo.setId(category.getId());
                    vo.setName(category.getName());
                    return vo;
                })
                .toList();
    }

    /**
     * 检查分类存在性和名称唯一性，然后更新
     * @param id 分类ID
     * @param dto 修改参数
     * @return
     */
    @Override
    @Transactional
    public CategoryVO update(Long id, CategoryUpdateDTO dto) {
        Category category = categoryMapper.selectById(id);

        if(category == null){
            throw new BusinessException(
                    "CATEGORY_NOT_FOUND",
                    "分类不存在"
            );
        }
        String name = dto.getName().strip();

        if(name.isBlank() || name.length() > 32){
            throw new BusinessException(
                    "INVALID_CATEGORY_NAME",
                    "分类名称不能为空且不能超过32个字符"
            );
        }
        if (categoryMapper.countByNameExcludingId(name,id) > 0){
            throw categoryAlreadyExists();
        }
        category.setName(name);

        try{
            categoryMapper.updateName(category);
        }catch (DuplicateKeyException ex){
            //唯一索引处理并发修改导致的名称冲突
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

    /**
     * 删除分类，不存在时抛出业务异常。
     *
     * @param id 分类ID
     */
    @Override
    @Transactional
    public void delete(Long id) {
        int deletedRows = categoryMapper.deleteById(id);

        if (deletedRows == 0) {
            throw new BusinessException(
                    "CATEGORY_NOT_FOUND",
                    "分类不存在"
            );
        }
    }



}
