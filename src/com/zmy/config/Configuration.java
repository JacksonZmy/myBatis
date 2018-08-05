package com.zmy.config;

import com.zmy.mapper.MapperProxy;
import com.zmy.session.SqlSession;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

public class Configuration {

    public <T> T getMapper(Class<T> clazz, SqlSession sqlSession) {
        return (T) Proxy.newProxyInstance(this.getClass().getClassLoader(),
                new Class[] {clazz},
                new MapperProxy(sqlSession));
    }

    /**
     * XML解析好了
     */
    public static class TestMapperXml{
        public static final String namesapce = "com.zmy.config.mappers.TestMapper";

        public static final Map<String, String> methodSqlMapping = new HashMap<>();

        static {
            methodSqlMapping.put("selectByPrimaryKey", "select * from test where id = %d");
        }
    }
}
