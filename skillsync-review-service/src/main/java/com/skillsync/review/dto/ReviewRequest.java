package com.skillsync.review.dto;

import lombok.Data;

@Data
public class ReviewRequest {

    private Long sessionId;
    private Integer rating;
    private String comment;
}