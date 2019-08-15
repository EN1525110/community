package com.xdx.community.dto;

import lombok.Data;

@Data
public class AccessTokenDTO {
    private String client_id;
    private String redirect_uri;
    private String state;
    private String code;
    private String client_secret;

}
