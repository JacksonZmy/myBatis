package com.zmy.config.mappers;

import com.zmy.beans.Test;

public interface TestMapper {

    Test selectByPrimaryKey(Integer userId);
}
