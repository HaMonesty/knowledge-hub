package com.knowledgehub.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class KnowledgeVO {
    private Long id;
    private String title;
    private String summary;
    private String content;
    private Long categoryId;
    private Long authorId;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}