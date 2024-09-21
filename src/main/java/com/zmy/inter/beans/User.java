package com.zmy.inter.beans;

import lombok.ToString;

@ToString
public class User {
    private Integer userId;

    private String addr;

    private String name;

    public static class Builder {

        private User user = new User();

        public Builder userId (Integer userId) {
            user.setUserId(userId);
            return this;
        }
        public Builder name (String name) {
            user.setName(name);
            return this;
        }
        public Builder addr (String addr) {
            user.setAddr(addr);
            return this;
        }

        public User build() {
            return user;
        }
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public User() {
    }

}