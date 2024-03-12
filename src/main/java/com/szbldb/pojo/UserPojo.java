package com.szbldb.pojo;

import lombok.Data;

@Data
public class UserPojo {
    private String username;
    private String password;
    private String jwtCode;
    private String code;
    private String jwtUser;
    private String email;
}
