package com.xdx.community.service;

import com.xdx.community.mapper.UserMapper;
import com.xdx.community.model.User;
import com.xdx.community.model.UserExample;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;


    public void saveOrUpdate(User user) {
        //1.现根据account_id查找user表看看用户是否存在
        UserExample userExample = new UserExample();
        userExample.createCriteria().andAccountIdEqualTo(user.getAccountId());
        List<User> users = userMapper.selectByExample(userExample);
        if(  users.size()== 0){
            //进行插入操作
            user.setGmtCreate(System.currentTimeMillis());
            user.setGmtModified(user.getGmtCreate());
            userMapper.insert(user);
        }
        else{
            //2.进行更新操作
            User u = users.get(0);
            u.setGmtModified(System.currentTimeMillis());
            u.setToken(user.getToken());
            u.setImageUrl(user.getImageUrl());
            u.setUserName(user.getUserName());
            u.setBio(user.getBio());
            userMapper.updateByPrimaryKey(u);
        }

    }
}
