package com.zmy;

import com.zmy.core.session.ZSqlSession;
import com.zmy.core.session.ZSqlSessionFactory;
import com.zmy.core.session.ZSqlSessionFactoryBuilder;
import com.zmy.inter.beans.User;
import com.zmy.inter.mapper.UserMapper;
import org.apache.ibatis.io.Resources;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class TestMain {
    public static void main(String[] args) throws IOException {
        String resource = "mybatis-config.xml";
        InputStream inputStream = Resources.getResourceAsStream(resource);
        ZSqlSessionFactory sqlSessionFactory = new ZSqlSessionFactoryBuilder().build(inputStream);
        ZSqlSession sqlSession = sqlSessionFactory.openSession();
//        User user = sqlSession.selectOne("com.zmy.inter.mapper.UserMapper.selectOne",1);
//        System.out.println(user.toString());
//        List<User> userList = sqlSession.selectList("com.zmy.inter.mapper.UserMapper.selectList", null);
//        int i = sqlSession.insert("com.zmy.inter.mapper.UserMapper.insert",
//                new User.Builder().addr("上海").name("Jackson").userId(3).build());
//        int i = sqlSession.update("com.zmy.inter.mapper.UserMapper.update",
//                new User.Builder().addr("天津").name("Jerry").userId(4).build());
        int i = sqlSession.delete("com.zmy.inter.mapper.UserMapper.delete",
                4);
        sqlSession.commit();

        System.out.println(i);


//        String resource = "mybatis-config.xml";
//        InputStream inputStream = Resources.getResourceAsStream(resource);
//        ZSqlSessionFactory sqlSessionFactory = new ZSqlSessionFactoryBuilder().build(inputStream);
//        ZSqlSession sqlSession = sqlSessionFactory.openSession();
////        UserMapper userMapper = sqlSession.getMapper(UserMapper.class);
////        User user = userMapper.selectOne(1);
//        UserMapper userMapper = sqlSession.getMapper(UserMapper.class);
//        List<User> userList = userMapper.selectList();
//        System.out.println(userList);
    }
}
