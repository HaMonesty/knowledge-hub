package com.knowledgehub.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class KnowledgeListVO {

    private Long id;
    private String title;
    private String summary;
    private Long categoryId;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}