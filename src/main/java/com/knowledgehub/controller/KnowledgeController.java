package com.knowledgehub.controller;

import com.knowledgehub.dto.KnowledgeDraftDTO;
import com.knowledgehub.service.KnowledgeService;
import com.knowledgehub.vo.KnowledgeVO;
import com.knowledgehub.vo.UserVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/knowledge")
@RequiredArgsConstructor
public class KnowledgeController {

    private final KnowledgeService knowledgeService;

    /**
     * 创建草稿，作者身份取自当前登录用户。
     *
     * @param dto 草稿内容
     * @param currentUser 当前登录用户
     * @return 创建后的草稿信息
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public KnowledgeVO create(
            @Valid @RequestBody KnowledgeDraftDTO dto,
            @AuthenticationPrincipal UserVO currentUser) {

        return knowledgeService.createDraft(
                dto,
                currentUser.getId()
        );
    }
    /**
     * 查看当前登录用户自己的草稿。
     *
     * @param id 知识ID
     * @param currentUser 当前登录用户
     * @return 草稿详情
     */
    @GetMapping("/{id}")
    public KnowledgeVO detail(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal UserVO currentUser) {

        return knowledgeService.getDraft(id, currentUser.getId());
    }

    /**
     * 编辑当前登录用户自己的草稿。
     *
     * @param id 知识ID
     * @param dto 修改后的草稿内容
     * @param currentUser 当前登录用户
     * @return 更新后的草稿
     */
    @PutMapping("/{id}")
    public KnowledgeVO update(
            @PathVariable("id") Long id,
            @Valid @RequestBody KnowledgeDraftDTO dto,
            @AuthenticationPrincipal UserVO currentUser) {

        return knowledgeService.updateDraft(
                id, dto, currentUser.getId()
        );
    }

    /**
     * 删除当前登录用户自己的草稿。
     *
     * @param id 知识ID
     * @param currentUser 当前登录用户
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal UserVO currentUser) {

        knowledgeService.deleteDraft(id, currentUser.getId());
    }
}