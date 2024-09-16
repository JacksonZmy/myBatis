package com.zmy;

import com.zmy.core.executor.ZSimpleExecutor;
import com.zmy.core.session.ZSqlSession;
import com.zmy.core.config.ZConfiguration;
import com.zmy.inter.beans.User;
import com.zmy.inter.mapper.UserMapper;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.InputStream;

public class TestMain {
    public static void main(String[] args) {
        ZSqlSession sqlSession = new ZSqlSession(new ZConfiguration(),
                new ZSimpleExecutor());
        UserMapper mapper = sqlSession.getMapper(UserMapper.class);
        User test = mapper.selectOne(2);
        System.out.println(test.toString());

        //        String resource = "mybatis-config.xml";
//        InputStream inputStream = Resources.getResourceAsStream(resource);
//        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
//        SqlSession sqlSession = sqlSessionFactory.openSession();
//        User user = sqlSession.selectOne("com.zmy.inter.mapper.UserMapper.selectOne",1);
//        System.out.println(user.toString());

//        String resource = "mybatis-config.xml";
//        InputStream inputStream = Resources.getResourceAsStream(resource);
//        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
//        SqlSession sqlSession = sqlSessionFactory.openSession();
//        UserMapper userMapper = sqlSession.getMapper(UserMapper.class);
//        User user = userMapper.selectOne(1);
//        System.out.println(user.toString());
    }
}
