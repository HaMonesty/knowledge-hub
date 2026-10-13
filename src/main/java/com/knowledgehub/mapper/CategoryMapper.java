package com.knowledgehub.mapper;


import com.knowledgehub.entity.Category;
import org.apache.ibatis.annotations.*;

@Mapper
public interface CategoryMapper {

    @Select("select count(*) from category where name = #{name}")
    long countByName(@Param("name") String name);

    @Insert("insert into category (name) VALUES  (#{name})")
    @Options(useGeneratedKeys = true,keyProperty = "id")
    int insert(Category category);

}
