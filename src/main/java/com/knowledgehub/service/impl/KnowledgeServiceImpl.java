package com.knowledgehub.service.impl;

import com.knowledgehub.dto.KnowledgeDraftDTO;
import com.knowledgehub.entity.Knowledge;
import com.knowledgehub.exception.BusinessException;
import com.knowledgehub.mapper.CategoryMapper;
import com.knowledgehub.mapper.KnowledgeMapper;
import com.knowledgehub.service.KnowledgeService;
import com.knowledgehub.vo.KnowledgeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.knowledgehub.common.PageResult;
import com.knowledgehub.vo.KnowledgeListVO;
import java.util.List;

import javax.swing.*;
import java.util.List;

@Service
@RequiredArgsConstructor
public class KnowledgeServiceImpl implements KnowledgeService {

    private final KnowledgeMapper knowledgeMapper;
    private final CategoryMapper categoryMapper;

    /**
     * 创建草稿，作者取自登录身份，状态固定为DRAFT。
     *
     * @param dto 草稿内容
     * @param authorId 当前登录用户ID
     * @return 创建后的草稿
     */
    @Override
    @Transactional
    public KnowledgeVO createDraft(KnowledgeDraftDTO dto, Long authorId) {
        validateCategory(dto.getCategoryId());

        Knowledge knowledge = new Knowledge();
        applyContent(knowledge, dto);
        knowledge.setAuthorId(authorId);
        knowledge.setStatus("DRAFT");

        knowledgeMapper.insert(knowledge);

        // 重新查询，取得数据库自动生成的创建、更新时间。
        return toVO(requireOwnDraft(knowledge.getId(), authorId));
    }

    /**
     * 在事务内锁定并编辑作者自己的草稿。
     *
     * @param id 知识ID
     * @param dto 草稿内容
     * @param authorId 当前登录用户ID
     * @return 更新后的草稿
     */
    @Override
    @Transactional
    public KnowledgeVO updateDraft(
            Long id, KnowledgeDraftDTO dto, Long authorId) {

        Knowledge knowledge =
                knowledgeMapper.selectOwnDraftForUpdate(id, authorId);

        if (knowledge == null) {
            throw knowledgeNotFound();
        }

        validateCategory(dto.getCategoryId());
        applyContent(knowledge, dto);
        knowledgeMapper.updateDraft(knowledge);

        return toVO(requireOwnDraft(id, authorId));
    }

    /**
     * 查询作者自己的草稿。
     *
     * @param id 知识ID
     * @param authorId 当前登录用户ID
     * @return 草稿详情
     */
    @Override
    public KnowledgeVO getDraft(Long id, Long authorId) {
        return toVO(requireOwnDraft(id, authorId));
    }

    /**
     * 检查可选分类是否存在。
     *
     * @param categoryId 分类ID，null表示不关联分类
     */
    private void validateCategory(Long categoryId) {
        if (categoryId != null
                && categoryMapper.selectById(categoryId) == null) {
            throw new BusinessException(
                    "CATEGORY_NOT_FOUND", "分类不存在"
            );
        }
    }

    /**
     * 替换可编辑字段，去除标题首尾空白。
     *
     * @param knowledge 知识实体
     * @param dto 请求内容
     */
    private void applyContent(
            Knowledge knowledge, KnowledgeDraftDTO dto) {

        String title = dto.getTitle().strip();

        if (title.isBlank() || title.length() > 200) {
            throw new BusinessException(
                    "INVALID_KNOWLEDGE_TITLE",
                    "标题不能为空且不能超过200个字符"
            );
        }

        knowledge.setTitle(title);
        knowledge.setSummary(dto.getSummary());
        knowledge.setContent(dto.getContent());
        knowledge.setCategoryId(dto.getCategoryId());
    }

    /**
     * 获取作者草稿，不存在和越权访问统一返回404。
     *
     * @param id 知识ID
     * @param authorId 当前用户ID
     * @return 可访问的草稿
     */
    private Knowledge requireOwnDraft(Long id, Long authorId) {
        Knowledge knowledge =
                knowledgeMapper.selectOwnDraft(id, authorId);

        if (knowledge == null) {
            throw knowledgeNotFound();
        }

        return knowledge;
    }

    /**
     * 构造知识不可访问异常，避免暴露他人的草稿是否存在。
     *
     * @return 知识不存在异常
     */
    private BusinessException knowledgeNotFound() {
        return new BusinessException(
                "KNOWLEDGE_NOT_FOUND", "知识不存在"
        );
    }

    /**
     * 将数据库实体转换为接口响应。
     *
     * @param knowledge 知识实体
     * @return 知识详情
     */
    private KnowledgeVO toVO(Knowledge knowledge) {
        KnowledgeVO vo = new KnowledgeVO();
        vo.setId(knowledge.getId());
        vo.setTitle(knowledge.getTitle());
        vo.setSummary(knowledge.getSummary());
        vo.setContent(knowledge.getContent());
        vo.setCategoryId(knowledge.getCategoryId());
        vo.setAuthorId(knowledge.getAuthorId());
        vo.setStatus(knowledge.getStatus());
        vo.setCreatedAt(knowledge.getCreatedAt());
        vo.setUpdatedAt(knowledge.getUpdatedAt());
        return vo;
    }

    /**
     * 删除自己的草稿，不可访问时统一返回不存在。
     *
     * @param id 知识ID
     * @param authorId 当前登录用户ID
     */
    @Override
    @Transactional
    public void deleteDraft(Long id, Long authorId) {
        int deletedRows = knowledgeMapper.deleteOwnDraft(id, authorId);

        if (deletedRows == 0) {
            throw knowledgeNotFound();
        }
    }

    /**
     * 校验分页条件，通过PageHelper查询当前用户的知识。
     *
     * @param authorId 当前登录用户ID
     * @param page 页码
     * @param size 每页条数
     * @param status 可选知识状态
     * @return 知识分页结果
     */

    @Override
    public PageResult<KnowledgeListVO> listMine(
            Long authorId, int page, int size, String status) {

        if (page < 1 || size < 1 || size > 50) {
            throw new BusinessException(
                    "INVALID_PAGE_PARAM",
                    "页码必须大于0，每页条数必须为1～50"
            );
        }
        if (status != null
                && !"DRAFT".equals(status)
                && !"PUBLISHED".equals(status)) {
            throw new BusinessException(
                    "INVALID_KNOWLEDGE_STATUS",
                    "知识状态只能为DRAFT或PUBLISHED"
            );
        }
        try{
            //下一条查询就是需要分页的目标查询
            PageHelper.startPage(page,size);
            List<KnowledgeListVO> records = knowledgeMapper.selectMine(authorId, status);

            //从原查询列表提取分页信息，保留真实总条数
            PageInfo<KnowledgeListVO> pageInfo =
                    new PageInfo<>(records);

            return new PageResult<>(
                    records,
                    pageInfo.getTotal(),
                    page,
                    size
            );

        }finally {
            PageHelper.clearPage();
        }
    }
}