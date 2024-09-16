package com.zmy.core.config.mapper;

import com.zmy.core.config.ZConfiguration;
import com.zmy.core.session.ZSqlSession;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author zmy
 */
public class ZMapperProxy implements InvocationHandler {

    private ZSqlSession sqlSession;

    public ZMapperProxy(ZSqlSession sqlSession) {
        this.sqlSession = sqlSession;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getDeclaringClass().getName().equals(ZConfiguration.TestMapperXml.namesapce)){
            String sql = ZConfiguration.TestMapperXml.methodSqlMapping.get(method.getName());
            return sqlSession.selectOne(sql, String.valueOf(args[0]));
        }
        return method.invoke(this, args);
    }
}
