package com.xdx.community.controller;

import com.xdx.community.dto.PaginationDTO;
import com.xdx.community.model.User;
import com.xdx.community.service.NotificationService;
import com.xdx.community.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

@Controller
public class ProfileController {


    @Autowired
    private QuestionService questionService;
    @Autowired
    private NotificationService notificationService;


    @GetMapping("/profile/{action}")
    public String profile(HttpServletRequest request, Model model,
                          @PathVariable(name = "action") String action,
                          @RequestParam(name = "page",defaultValue = "1") Integer page,
                          @RequestParam(name = "pageSize",defaultValue = "5") Integer pageSize){
        User user = (User) request.getSession().getAttribute("user");
        if(user == null ){
            return "redirect:/";
        }
        //查出当前用户未读消息数并放到session 域中
        Long unreadCount = notificationService.unreadCount(user.getId());
        request.getSession().setAttribute("unreadCount",unreadCount);

        if ("questions".equals(action)){
            model.addAttribute("sessionType","questions");
            model.addAttribute("sessionName","我的提问");
            PaginationDTO paginationDTO = questionService.getQuestByCreatorID(user.getId(), page, pageSize);
            model.addAttribute("paginationDTO",paginationDTO);
        }
        if ("replies".equals(action)){
            //查找回复列表
            PaginationDTO paginationDTO = notificationService.list(user.getId(), page, pageSize);
            model.addAttribute("sessionType", "replies");
            model.addAttribute("paginationDTO", paginationDTO);
            model.addAttribute("sessionName", "最新回复");
        }
        return "profile";
    }
}
