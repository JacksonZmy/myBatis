package com.zmy.core.config;

import com.zmy.core.config.mapper.ZMapperProxy;
import com.zmy.core.session.ZSqlSession;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

public class ZConfiguration {

    public <T> T getMapper(Class<T> clazz, ZSqlSession sqlSession) {
        return (T) Proxy.newProxyInstance(this.getClass().getClassLoader(),
                new Class[] {clazz},
                new ZMapperProxy(sqlSession));
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
