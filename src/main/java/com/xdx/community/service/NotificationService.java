package com.xdx.community.service;

import com.xdx.community.dto.NotificationDTO;
import com.xdx.community.dto.PaginationDTO;
import com.xdx.community.enums.NotificationStatusEnum;
import com.xdx.community.enums.NotificationTypeEnum;
import com.xdx.community.exception.CustomizeErrorCode;
import com.xdx.community.exception.CustomizeException;
import com.xdx.community.mapper.NotificationMapper;
import com.xdx.community.model.Notification;
import com.xdx.community.model.NotificationExample;
import com.xdx.community.model.User;
import org.apache.ibatis.session.RowBounds;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


@Service
public class NotificationService {

    @Autowired
    private NotificationMapper notificationMapper;

    /**
     * 分页查看消息
     * @param userId
     * @param page
     * @param size
     * @return
     */
    public PaginationDTO list(Long userId, Integer page, Integer size) {
        PaginationDTO<NotificationDTO> paginationDTO = new PaginationDTO<>();

        Integer totalPage;

        //根据当前登陆用户作为接收者id查看自己的消息总数
        NotificationExample notificationExample = new NotificationExample();
        notificationExample.createCriteria()
                .andReceiverEqualTo(userId);
        Integer totalCount = (int) notificationMapper.countByExample(notificationExample);

        if (totalCount % size == 0) {
            totalPage = totalCount / size;
        } else {
            totalPage = totalCount / size + 1;
        }

        if (page < 1) {
            page = 1;
        }
        if (page > totalPage) {
            page = totalPage;
        }

        paginationDTO.setPagination(totalPage, page);

        //size*(page-1)
        Integer offset = size * (page - 1);

        //倒叙排序查找当前用户的消息列表【有分页】
        NotificationExample example = new NotificationExample();
        example.createCriteria()
                .andReceiverEqualTo(userId);
        example.setOrderByClause("gmt_create desc");
        List<Notification> notifications = notificationMapper.selectByExampleWithRowbounds(example, new RowBounds(offset, size));

        if (notifications.size() == 0) {
            //如果没查到记录，直接返回paginationDTO
            return paginationDTO;
        }

        List<NotificationDTO> notificationDTOS = new ArrayList<>();
        for (Notification notification : notifications) {
            //有查到记录，将消息进行封装到NotificationDTO，然后保存到集合中
            NotificationDTO notificationDTO = new NotificationDTO();
            BeanUtils.copyProperties(notification, notificationDTO);
            notificationDTO.setTypeName(NotificationTypeEnum.nameOfType(notification.getType()));
            notificationDTOS.add(notificationDTO);
        }
        paginationDTO.setData(notificationDTOS);
        return paginationDTO;
    }

    /**
     *  查找当前用户未读取的消息总数
     * @param userId
     * @return
     */
    public Long unreadCount(Long userId) {
        NotificationExample notificationExample = new NotificationExample();
        notificationExample.createCriteria()
                .andReceiverEqualTo(userId)
                .andStatusEqualTo(NotificationStatusEnum.UNREAD.getStatus());
        return notificationMapper.countByExample(notificationExample);
    }

    public NotificationDTO read(Long id, User user) {
        //通过主键id查找通知类实体对象
        Notification notification = notificationMapper.selectByPrimaryKey(id);
        //如果为空，抛出异常 ;  NOTIFICATION_NOT_FOUND(2009, "消息莫非是不翼而飞了？"),
        if (notification == null) {
            throw new CustomizeException(CustomizeErrorCode.NOTIFICATION_NOT_FOUND);
        }
        //判断当前用户是否和消息的接收者id一致，不一致抛出异常，不能读取别人的消息
        // READ_NOTIFICATION_FAIL(2008, "兄弟你这是读别人的信息呢？"),
        if (!Objects.equals(notification.getReceiver(), user.getId())) {
            throw new CustomizeException(CustomizeErrorCode.READ_NOTIFICATION_FAIL);
        }

        //设置消息状态是否已经读取 ，也就是1=已经读取消息
        notification.setStatus(NotificationStatusEnum.READ.getStatus());
        //对消息进行更新
        notificationMapper.updateByPrimaryKey(notification);

        NotificationDTO notificationDTO = new NotificationDTO();
        BeanUtils.copyProperties(notification, notificationDTO);
        notificationDTO.setTypeName(NotificationTypeEnum.nameOfType(notification.getType()));
        return notificationDTO;
    }
}
