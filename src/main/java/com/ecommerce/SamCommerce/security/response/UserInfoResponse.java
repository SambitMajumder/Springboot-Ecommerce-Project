package com.ecommerce.SamCommerce.security.response;

import java.util.List;

public class UserInfoResponse {
    private Integer id;
    private String jwtToken;
    private String userName;
    private List<String> roles;

    public UserInfoResponse(Integer id, List<String> roles, String userName, String jwtToken) {
        this.roles = roles;
        this.id = id;
        this.userName = userName;
        this.jwtToken = jwtToken;
    }

    public UserInfoResponse(Integer id, List<String> roles, String userName) {
        this.roles = roles;
        this.id = id;
        this.userName = userName;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getJwtToken() {
        return jwtToken;
    }

    public void setJwtToken(String jwtToken) {
        this.jwtToken = jwtToken;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
}
