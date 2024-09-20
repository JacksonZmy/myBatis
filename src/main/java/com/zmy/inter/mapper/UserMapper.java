package com.zmy.inter.mapper;

import com.zmy.inter.beans.User;

import java.util.List;

public interface UserMapper {

    User selectOne(Integer id);

    List<User> selectList();


}
