package com.xdx.community.controller;

import com.xdx.community.dto.CommentDto;
import com.xdx.community.dto.ResultDTO;
import com.xdx.community.enums.CommentTypeEnum;
import com.xdx.community.exception.CustomizeErrorCode;
import com.xdx.community.exception.CustomizeException;
import com.xdx.community.model.Comment;
import com.xdx.community.model.User;
import com.xdx.community.service.CommentService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
public class CommentController {

    @Autowired
    private CommentService commentService;

    @ResponseBody
    @RequestMapping(value = "/comment",method = RequestMethod.POST)
    public ResultDTO comment(@RequestBody CommentDto commentDto, HttpServletRequest request){


        User user = (User) request.getSession().getAttribute("user");
        if( user == null ){
            //当前是未登录
            throw  new CustomizeException(CustomizeErrorCode.NO_LOGIN);
        }
        if( commentDto==null || commentDto.getContent()==null || commentDto.getContent().trim()==""){
            //评论内容不能为空
            throw  new CustomizeException(CustomizeErrorCode.CONTENT_IS_EMPTY);
        }
        //获取session用户的id
        Comment comment = new Comment();
        comment.setParentId(commentDto.getParentId());
        comment.setCommentator(user.getId());
        comment.setGmtCreate(System.currentTimeMillis());
        comment.setGmtModified(System.currentTimeMillis());
        comment.setContent(commentDto.getContent());
        comment.setType(commentDto.getType());
        comment.setLikeCount(0L);
        //添加到数据库中
        commentService.insertComment(comment,user);
        return ResultDTO.okOf();
    }
    /**
     * 获取二级评论
     */
    @ResponseBody
    @RequestMapping(value = "/comment/{id}",method = RequestMethod.GET)
    public ResultDTO<List<CommentDto>> get2LevelComment(@PathVariable("id")Long id){
        List<CommentDto> listByQid = commentService.getListByQid(id, CommentTypeEnum.COMMENT);
        return ResultDTO.okOf(listByQid);
    }

}
