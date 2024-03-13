package com.szbldb.pojo.userInfoPojo;

import lombok.Data;

@Data
public class UserInfo {
    private Integer id;
    private String name;
    private String roles;
    private String introduction;
    private String avatar;

    @Override
    public String toString() {
        return "UserInfo{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", roles='" + roles + '\'' +
                ", introduction='" + introduction + '\'' +
                ", avatar='" + avatar + '\'' +
                '}';
    }
}
