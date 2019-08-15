package com.xdx.community.controller;

import com.xdx.community.cache.TagCache;
import com.xdx.community.model.Question;
import com.xdx.community.model.User;
import com.xdx.community.service.QuestionService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import javax.servlet.http.HttpServletRequest;

@Controller
public class PublishController {

    @Autowired
    private QuestionService questionService;

    @GetMapping("/publish/{id}")
    public String edit(@PathVariable("id") Long id,Model model){
        Question question = questionService.getById(id);
        model.addAttribute("title",question.getTitle());
        model.addAttribute("question_desc",question.getQuestionDesc());
        model.addAttribute("tags",question.getTags());
        model.addAttribute("id",question.getId());
        //添加问题标签库信息
        model.addAttribute("qtags", TagCache.get());
        return "publish";
    }

    @GetMapping("/publish")
    public String publish(Model model){
        //添加问题标签库信息
        model.addAttribute("qtags", TagCache.get());
        return "publish";
    }

    @PostMapping("publish")
    public String doPublish(
            @RequestParam(value="title",required = false) String title,
            @RequestParam(value="question_desc",required = false) String question_desc,
            @RequestParam(value="tags",required = false) String tags,
            @RequestParam(value="id",required = false) Long id,
            HttpServletRequest request, Model model){

        model.addAttribute("title",title);
        model.addAttribute("question_desc",question_desc);
        model.addAttribute("tags",tags);
        //添加问题标签库信息
        model.addAttribute("qtags", TagCache.get());




        //添加问题的标签库可选项
        model.addAttribute("qtags", TagCache.get());

        if(title==null || title==""){ model.addAttribute("error","标题不能为空");return "publish";}
        if(question_desc==null || question_desc==""){ model.addAttribute("error","问题补充不能为空");return "publish";}
        if(tags==null || tags==""){ model.addAttribute("error","标签不能为空");return "publish";}
        //标签是否贵方
        String invalid = TagCache.filterInvalid(tags);
        if (StringUtils.isNotBlank(invalid)) {
            model.addAttribute("error", "输入非法标签:" + invalid);
            return "publish";
        }

        //判断用户是否登录
        User user = (User) request.getSession().getAttribute("user");
        if( user == null ){
            model.addAttribute("error","用户未登录");
            return "publish";
        }

        Question question = new Question();
        if( id!=null){
            question.setId(id);
        }
        question.setTitle(title);
        question.setTags(tags);
        question.setQuestionDesc(question_desc);
        question.setCreator(user.getId());

        questionService.createOrUpdate(question);
        return "redirect:/";
    }
}
