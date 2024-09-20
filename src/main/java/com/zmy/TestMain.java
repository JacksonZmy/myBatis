package com.zmy;

import com.zmy.core.session.ZSqlSession;
import com.zmy.core.session.ZSqlSessionFactory;
import com.zmy.core.session.ZSqlSessionFactoryBuilder;
import com.zmy.inter.beans.User;
import com.zmy.inter.mapper.UserMapper;
import org.apache.ibatis.io.Resources;

import java.io.IOException;
import java.io.InputStream;

public class TestMain {
    public static void main(String[] args) throws IOException {
//        String resource = "mybatis-config.xml";
//        InputStream inputStream = Resources.getResourceAsStream(resource);
//        ZSqlSessionFactory sqlSessionFactory = new ZSqlSessionFactoryBuilder().build(inputStream);
//        ZSqlSession sqlSession = sqlSessionFactory.openSession();
//        User user = sqlSession.selectOne("com.zmy.inter.mapper.UserMapper.selectOne",1);
//        System.out.println(user.toString());

        String resource = "mybatis-config.xml";
        InputStream inputStream = Resources.getResourceAsStream(resource);
        ZSqlSessionFactory sqlSessionFactory = new ZSqlSessionFactoryBuilder().build(inputStream);
        ZSqlSession sqlSession = sqlSessionFactory.openSession();
        UserMapper userMapper = sqlSession.getMapper(UserMapper.class);
        User user = userMapper.selectOne(1);
        System.out.println(user.toString());
    }
}
