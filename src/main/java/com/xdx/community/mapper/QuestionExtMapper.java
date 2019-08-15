package com.xdx.community.mapper;

import com.xdx.community.dto.QuestionQueryDTO;
import com.xdx.community.model.Question;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface QuestionExtMapper {
    //增加浏览
    void addViewCount(Question question);

    //增加回复
    void addCommentCount(Question question);

    //查找相关问题
    List<Question> selectRelated(Question question);

    //根据关键字查找总数
    Integer countBySearch(QuestionQueryDTO questionQueryDTO);

    //根据关键字查找列表
    List<Question> selectBySearch(QuestionQueryDTO questionQueryDTO);
}