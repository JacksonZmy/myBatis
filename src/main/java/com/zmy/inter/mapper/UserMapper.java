package com.zmy.inter.mapper;

import com.zmy.inter.beans.User;

import java.util.List;

public interface UserMapper {

    User selectOne(Integer id);

    List<User> selectList();

    int insert(User user);

    int update(User user);

    int delete(int userId);

}
