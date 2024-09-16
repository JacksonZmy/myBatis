package com.zmy.core.session;

import com.zmy.core.config.ZConfiguration;
import com.zmy.core.executor.ZExcutor;

public class ZSqlSession {

    private ZConfiguration configuration;
    private ZExcutor excutor;

    public ZSqlSession(ZConfiguration configuration, ZExcutor excutor){
        this.configuration = configuration;
        this.excutor = excutor;
    }


    /**
     * getMapper
     * @param clazz
     */
    public <T> T getMapper(Class<T> clazz){
        return configuration.getMapper(clazz, this);
    }


    /**
     * @param statement sql语句
     * @param parameter sql参数
     * @param <T>
     * @return
     */
    public <T> T selectOne(String statement, String parameter){
        return excutor.query(statement, parameter);
    }
}
