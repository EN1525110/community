package com.xdx.community.controller;


import com.xdx.community.dto.PaginationDTO;
import com.xdx.community.dto.QuestionDto;
import com.xdx.community.mapper.UserMapper;
import com.xdx.community.model.User;
import com.xdx.community.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
public class IndexController {

    @Autowired
    private QuestionService questionService;

    /**
     *
     * @param request
     * @param model
     * @param page  当前页数是多少,默认值是1
     * @param pageSize：每页显示数目.默认值是5
     * @return
     */
    @RequestMapping(value = {"/","/index.do"})
    public String index(Model model,
                        @RequestParam(value = "page",defaultValue = "1") Integer page,
                        @RequestParam(value = "pageSize",defaultValue = "2") Integer pageSize,
                         @RequestParam(value = "search",required = false) String search ){

        //获取问题列表到首页
        PaginationDTO paginationDTO = questionService.questionList(search,page, pageSize);
        model.addAttribute("paginationDTO",paginationDTO);
        model.addAttribute("search",search);
        return "index";
    }
}
