package com.zmy;

import com.zmy.inter.beans.User;
import com.zmy.inter.mapper.UserMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

@ContextConfiguration(locations = {"classpath:spring.xml"})
@RunWith(value = SpringJUnit4ClassRunner.class)
public class MybatisSpringTest {

    @Autowired
    private UserMapper userMapper;

    @Test
    public void testQuery(){
        List<User> list = userMapper.selectList();
        list.forEach(user -> {
            System.out.println(user.toString());
        });
    }
}
