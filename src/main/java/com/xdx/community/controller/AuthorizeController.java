package com.xdx.community.controller;

import com.xdx.community.dto.AccessTokenDTO;
import com.xdx.community.mapper.UserMapper;
import com.xdx.community.model.User;
import com.xdx.community.provider.GithubProvider;
import com.xdx.community.provider.GithubUser;
import com.xdx.community.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

@Controller
public class AuthorizeController {
    @Autowired
    private GithubProvider provider;

    @Autowired
    private UserService userService;
    @Value("${github.clinet.id}")
    private String clientId;
    @Value("${github.client.secret}")
    private String cilent_secret;
    @Value("${github.redirect.uri}")
    private String redirect_uri;

    @RequestMapping("/callback")
    public String callback(@RequestParam(name="state")String state, @RequestParam(name="code")String code, HttpServletRequest request, HttpServletResponse response){
        AccessTokenDTO dto = new AccessTokenDTO();
        dto.setClient_id(clientId);
        dto.setState(state);
        dto.setCode(code);
        dto.setRedirect_uri(redirect_uri);
        dto.setClient_secret(cilent_secret);

        String token = provider.getToken(dto);
        System.out.println(token);
        //根据token去获取那个user
        GithubUser githubUser = provider.getUser(token);
        if(githubUser != null){
            //System.out.println(user);
            User user = new User();
            user.setAccountId(String.valueOf(githubUser.getId()));
            user.setUserName(githubUser.getName());
            user.setToken(token);
            user.setBio(githubUser.getBio());
            user.setImageUrl(githubUser.getAvatarUrl());
            userService.saveOrUpdate(user);
            response.addCookie(new Cookie("token",token));
            return "redirect:/";
        }
        else{
            //登陆失败
            return "redirect:/";
        }
    }

    //退出登录
    @RequestMapping("/logout")
    public String logout(HttpServletRequest request,HttpServletResponse response){
        //移除cookie
        request.getSession().removeAttribute("user");
        //移除cookie
        Cookie cookie = new Cookie("token",null);
        cookie.setMaxAge(1);
        response.addCookie(cookie);
        return "redirect:/";
    }
}
