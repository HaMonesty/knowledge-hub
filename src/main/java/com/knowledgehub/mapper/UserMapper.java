package com.knowledgehub.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.knowledgehub.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    @Select("SELECT COUNT(*) FROM sys_user WHERE username = #{username}")
    Long countByUsername(@Param("username") String username);

    @Select("""
    SELECT id, username, password_hash, nickname, role, status,
           created_at, updated_at
    FROM sys_user
    WHERE username = #{username}
    """)
    User selectByUsername(@Param("username") String username);
}