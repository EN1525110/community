package com.xdx.community.service;

import com.xdx.community.dto.CommentDto;
import com.xdx.community.enums.CommentTypeEnum;
import com.xdx.community.enums.NotificationStatusEnum;
import com.xdx.community.enums.NotificationTypeEnum;
import com.xdx.community.exception.CustomizeErrorCode;
import com.xdx.community.exception.CustomizeException;
import com.xdx.community.mapper.*;
import com.xdx.community.model.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
public class CommentService {
    @Autowired
    CommentMapper commentMapper;
    @Autowired
    QuestionMapper questionMapper;
    @Autowired
    QuestionExtMapper questionExtMapper;
    @Autowired
    UserMapper userMapper;
    @Autowired
    private NotificationMapper notificationMapper;
    @Autowired
    private CommentExtMapper commentExtMapper;

    @Transactional
    public void insertComment(Comment comment,User commentator) {
        //未选中任何问题或者评论不能进行回复
        if(comment.getParentId()==null ||  comment.getParentId()==0 ){  throw  new CustomizeException(CustomizeErrorCode.TARGET_PARAM_NOT_FOUND); }
        //TYPE_PARAM_WRONG(2005, "评论类型错误或不存在"),
        if( comment.getType()==null ||  !CommentTypeEnum.isExist(comment.getType())){ throw  new CustomizeException(CustomizeErrorCode.TYPE_PARAM_WRONG); }

        if(comment.getType() ==CommentTypeEnum.COMMENT.getType()){
            //回复评论
            Comment dbComment = commentMapper.selectByPrimaryKey(comment.getParentId());
            if(dbComment==null){ //说明该条评论已经被删除
                throw new CustomizeException(CustomizeErrorCode.COMMENT_NOT_FOUND);
            }
            //对评论进行回复
            commentMapper.insert(comment);
            // 增加评论数
            Comment parentComment = new Comment();
            parentComment.setId(comment.getParentId());
            parentComment.setCommentCount(1);//每次增加1
            commentExtMapper.incCommentCount(parentComment);
            Question question = questionMapper.selectByPrimaryKey(dbComment.getParentId());
            if (question == null) {
                throw new CustomizeException(CustomizeErrorCode.QUESTION_NOT_FOUND);
            }
            // 创建通知
            createNotify(comment, dbComment.getCommentator(), commentator.getUserName(), question.getTitle(), NotificationTypeEnum.REPLY_COMMENT, question.getId());
        }else{
            //回复问题
            //通过parenteId找到问题实体
            Question question = questionMapper.selectByPrimaryKey(comment.getParentId());
            if( question==null ){
                //问题i不存在了
                throw new CustomizeException(CustomizeErrorCode.QUESTION_NOT_FOUND);
            }else{
                //对问题进行回复
                commentMapper.insert(comment);
                //对问题进行回复数加上1
                question.setCommentCount(1L);
                questionExtMapper.addCommentCount(question);
                // 创建通知
                createNotify(comment, question.getCreator(), commentator.getUserName(), question.getTitle(), NotificationTypeEnum.REPLY_QUESTION, question.getId());
            }
        }
    }

    /**
     * 查找问题的评论信息
     * @param id
     * @return
     */
    public List<CommentDto> getListByQid(Long id, CommentTypeEnum commentTypeEnum) {
        List<CommentDto> commentDtoList = new ArrayList<>();
        CommentExample commentExample = new CommentExample();
        commentExample.createCriteria()
                .andParentIdEqualTo(id)
                .andTypeEqualTo(commentTypeEnum.getType());
        List<Comment> comments = commentMapper.selectByExample(commentExample);
        if( comments.size()==0){
            //当前问题没有评论信息
            return commentDtoList;
        }
        //每一个评论都有评论人id
        for (Comment comment:comments ) {
            CommentDto dto = new CommentDto();
            BeanUtils.copyProperties(comment,dto);
            Long commentator = comment.getCommentator();
            User user =  userMapper.selectByPrimaryKey(commentator);
            dto.setUser(user);
            commentDtoList.add(dto);
        }
        return commentDtoList;
    }

    /**
     *
     * @param comment 回复或者评价实体
     * @param receiver ：接收者
     * @param notifierName 发送者
     * @param outerTitle outerTitle
     * @param notificationType
     * @param outerId
     */
    private void createNotify(Comment comment, Long receiver, String notifierName, String outerTitle, NotificationTypeEnum notificationType, Long outerId) {

       //发送者等于接收者，不记录通知.开发阶段暂不打开
        /*if (receiver == comment.getCommentator()) {
            return;
        }*/
        Notification notification = new Notification();
        notification.setGmtCreate(System.currentTimeMillis());
        notification.setType(notificationType.getType());
        notification.setOuterid(outerId);
        notification.setNotifier(comment.getCommentator());
        notification.setStatus(NotificationStatusEnum.UNREAD.getStatus());
        notification.setReceiver(receiver);
        notification.setNotifierName(notifierName);
        notification.setOuterTitle(outerTitle);
        notificationMapper.insert(notification);
    }
}
