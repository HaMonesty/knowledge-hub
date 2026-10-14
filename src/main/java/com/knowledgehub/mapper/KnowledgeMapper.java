package com.knowledgehub.mapper;

import com.knowledgehub.entity.Knowledge;
import org.apache.ibatis.annotations.*;
import com.knowledgehub.vo.KnowledgeListVO;
import java.util.List;

@Mapper
public interface KnowledgeMapper {

    /**
     * 插入草稿，并将数据库生成的ID回填到实体。
     *
     * @param knowledge 草稿实体
     * @return 插入行数
     */
    @Insert("""
            INSERT INTO knowledge
                (title, summary, content, category_id, author_id, status)
            VALUES
                (#{title}, #{summary}, #{content},
                 #{categoryId}, #{authorId}, #{status})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Knowledge knowledge);

    /**
     * 查询指定作者的草稿。
     *
     * @param id 知识ID
     * @param authorId 当前用户ID
     * @return 草稿；不存在或不属于该作者时返回null
     */
    @Select("""
            SELECT id, title, summary, content,
                   category_id AS categoryId,
                   author_id AS authorId, status,
                   created_at AS createdAt,
                   updated_at AS updatedAt
            FROM knowledge
            WHERE id = #{id}
              AND author_id = #{authorId}
              AND status = 'DRAFT'
            """)
    Knowledge selectOwnDraft(
            @Param("id") Long id,
            @Param("authorId") Long authorId
    );

    /**
     * 锁定作者的草稿，避免编辑过程中被并发删除或改变状态。
     *
     * @param id 知识ID
     * @param authorId 当前用户ID
     * @return 被锁定草稿；不可访问时返回null
     */
    @Select("""
            SELECT id, title, summary, content,
                   category_id AS categoryId,
                   author_id AS authorId, status,
                   created_at AS createdAt,
                   updated_at AS updatedAt
            FROM knowledge
            WHERE id = #{id}
              AND author_id = #{authorId}
              AND status = 'DRAFT'
            FOR UPDATE
            """)
    Knowledge selectOwnDraftForUpdate(
            @Param("id") Long id,
            @Param("authorId") Long authorId
    );

    /**
     * 更新草稿可编辑字段，不允许修改作者和状态。
     *
     * @param knowledge 草稿实体
     * @return 数据库报告的影响行数
     */
    @Update("""
            UPDATE knowledge
            SET title = #{title},
                summary = #{summary},
                content = #{content},
                category_id = #{categoryId}
            WHERE id = #{id}
              AND author_id = #{authorId}
              AND status = 'DRAFT'
            """)
    int updateDraft(Knowledge knowledge);

    /**
     * 删除作者自己的草稿。
     *
     * @param id 知识ID
     * @param authorId 当前登录用户ID
     * @return 删除行数
     */
    @Delete("""
        DELETE FROM knowledge
        WHERE id = #{id}
          AND author_id = #{authorId}
          AND status = 'DRAFT'
        """)
    int deleteOwnDraft(
            @Param("id") Long id,
            @Param("authorId") Long authorId
    );

    /**
     * 查询当前用户的知识，可按状态筛选。
     *
     * @param authorId 当前登录用户ID
     * @param status 状态，null表示不筛选
     * @return 知识列表
     */
    List<KnowledgeListVO> selectMine(
            @Param("authorId") Long authorId,
            @Param("status") String status
    );

}