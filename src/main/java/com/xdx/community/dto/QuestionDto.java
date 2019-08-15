package com.xdx.community.dto;

import com.xdx.community.model.User;
import lombok.Data;

@Data
public class QuestionDto {
    private Long id;
    private String title;
    private String questionDesc;
    private Long gmtCreate;
    private Long gmtModified;
    private Long creator;
    private Long commentCount;
    private Long viewCount;
    private Long likeCount;
    private String tags;
    private User user;
}
