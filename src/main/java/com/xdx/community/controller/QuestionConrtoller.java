package com.xdx.community.controller;

import com.xdx.community.dto.CommentDto;
import com.xdx.community.dto.QuestionDto;
import com.xdx.community.enums.CommentTypeEnum;
import com.xdx.community.service.CommentService;
import com.xdx.community.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
public class QuestionConrtoller {

    @Autowired
    private QuestionService questionService;

    @Autowired
    private CommentService commentService;

    @GetMapping("/question/{id}")
    public String questionInfo(@PathVariable("id") Long id, Model model){

        //根据问题id查找
        QuestionDto questionbyId = questionService.getQuestionbyId(id);
        //增加累计浏览量【阅读数】
        questionService.addViewCount(id);


        //查找问题对应的评论
        List<CommentDto> commentDto = commentService.getListByQid(id, CommentTypeEnum.QUESTION);

        //查找相关问题，相关问题是根据标签来完成的

        List<QuestionDto> relatedQuestions = questionService.selectRelated(questionbyId);
        //List<CommentDto> comments = commentService.listByTargetId(id, CommentTypeEnum.QUESTION);
        model.addAttribute("question",questionbyId);
        model.addAttribute("commentDto",commentDto);
        model.addAttribute("relatedQuestions",relatedQuestions);
        return "question";
    }
}
