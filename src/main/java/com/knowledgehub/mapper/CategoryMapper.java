package com.knowledgehub.mapper;


import com.knowledgehub.entity.Category;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CategoryMapper {

    @Select("select count(*) from category where name = #{name}")
    long countByName(@Param("name") String name);

    @Insert("insert into category (name) VALUES  (#{name})")
    @Options(useGeneratedKeys = true,keyProperty = "id")
    int insert(Category category);

    /**
     * 查询所有分类，按ID升序排列。
     *
     * @return 分类列表，无数据时返回空列表
     */
    @Select("""
        SELECT id, name
        FROM category
        ORDER BY id ASC
        """)
    List<Category> selectAll();


    /**
     * 根据ID查询分类。
     *
     * @param id 分类ID
     * @return 分类；不存在时返回null
     */
    @Select("SELECT id, name FROM category WHERE id = #{id}")
    Category selectById(@Param("id") Long id);

    /**
     * 检查其他分类是否已使用该名称，排除当前分类。
     *
     * @param name 分类名称
     * @param id 当前分类ID
     * @return 同名分类数量
     */
    @Select("""
        SELECT COUNT(*)
        FROM category
        WHERE name = #{name} AND id <> #{id}
        """)
    long countByNameExcludingId(
            @Param("name") String name,
            @Param("id") Long id
    );

    /**
     * 修改分类名称。
     *
     * @param category 包含分类ID和新名称的实体
     * @return SQL执行后报告的影响行数
     */
    @Update("""
        UPDATE category
        SET name = #{name}
        WHERE id = #{id}
        """)
    int updateName(Category category);

    /**
     * 根据ID删除分类。
     *
     * @param id 分类ID
     * @return 删除行数，0表示分类不存在
     */
    @Delete("DELETE FROM category WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

}
