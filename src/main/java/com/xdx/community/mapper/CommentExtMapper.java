package com.xdx.community.mapper;


import com.xdx.community.model.Comment;

public interface CommentExtMapper {
    int incCommentCount(Comment comment);
}