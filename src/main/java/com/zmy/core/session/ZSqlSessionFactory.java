package com.zmy.core.session;

import org.apache.ibatis.session.TransactionIsolationLevel;

/**
 *  获得一个 connection 或 DataSource
 */
public interface ZSqlSessionFactory {

    ZSqlSession openSession();
    ZSqlSession openSession(boolean autoCommit);
    ZSqlSession openSession(ZExecutorType execType);
    ZSqlSession openSession(TransactionIsolationLevel level);
}
