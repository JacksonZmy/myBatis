package com.zmy.core.session;

/**
 *  获得一个 connection 或 DataSource
 */
public interface ZSqlSessionFactory {

    ZSqlSession openSession();
}
