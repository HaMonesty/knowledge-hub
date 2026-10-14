package com.knowledgehub.service;

import com.knowledgehub.dto.KnowledgeDraftDTO;
import com.knowledgehub.vo.KnowledgeVO;

public interface KnowledgeService {

    /**
     * 创建当前用户的草稿。
     *
     * @param dto 草稿内容
     * @param authorId 当前登录用户ID
     * @return 创建后的草稿
     */
    KnowledgeVO createDraft(KnowledgeDraftDTO dto, Long authorId);

    /**
     * 编辑当前用户的草稿。
     *
     * @param id 知识ID
     * @param dto 草稿内容
     * @param authorId 当前登录用户ID
     * @return 更新后的草稿
     */
    KnowledgeVO updateDraft(Long id, KnowledgeDraftDTO dto, Long authorId);

    /**
     * 查看当前用户的草稿。
     *
     * @param id 知识ID
     * @param authorId 当前登录用户ID
     * @return 草稿详情
     */
    KnowledgeVO getDraft(Long id, Long authorId);

    /**
     * 删除当前用户自己的草稿。
     *
     * @param id 知识ID
     * @param authorId 当前登录用户ID
     */
    void deleteDraft(Long id, Long authorId);
}