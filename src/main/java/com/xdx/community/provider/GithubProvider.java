package com.xdx.community.provider;

import com.alibaba.fastjson.JSON;
import com.xdx.community.dto.AccessTokenDTO;
import okhttp3.*;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class GithubProvider {

    public String   getToken(AccessTokenDTO dto){

        //使用OKHttp发送post请求
        MediaType mediaType = MediaType.get("application/json; charset=utf-8");

        OkHttpClient client = new OkHttpClient();

            RequestBody body = RequestBody.create(mediaType, JSON.toJSONString(dto));
            Request request = new Request.Builder()
                    .url("https://github.com/login/oauth/access_token")
                    .post(body)
                    .build();
            try (Response response = client.newCall(request).execute()) {
                //获取到token,可根据这个token去获取到githubuser
                String str = response.body().string();
                String token = str.split("&")[0].split("=")[1];
                return token;
            } catch (IOException e) {
            }
        return null;
    }

    public GithubUser getUser(String access_token){
        //使用OkHttp发送一个get请求
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://api.github.com/user?access_token=" + access_token)
                .build();
        try {
            Response response = client.newCall(request).execute();
            String string = response.body().string();
            GithubUser githubUser = JSON.parseObject(string, GithubUser.class);
            return githubUser;
        } catch (Exception e) {
        }
        return null;
    }
}
