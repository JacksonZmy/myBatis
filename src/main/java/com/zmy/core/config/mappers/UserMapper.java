package com.zmy.core.config.mappers;

import com.zmy.inter.beans.User;

public interface UserMapper {

    User selectById(Integer userId);
}
