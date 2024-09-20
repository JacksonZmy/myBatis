package com.zmy.core.session.defaults;

import com.zmy.core.session.ZConfiguration;
import com.zmy.core.executor.ZExecutor;
import com.zmy.core.mapping.ZEnvironment;
import com.zmy.core.session.ZExecutorType;
import com.zmy.core.session.ZSqlSession;
import com.zmy.core.session.ZSqlSessionFactory;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.TransactionIsolationLevel;
import org.apache.ibatis.transaction.Transaction;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.managed.ManagedTransactionFactory;

public class ZDefaultSqlSessionFactory implements ZSqlSessionFactory {

    private final ZConfiguration configuration;

    public ZDefaultSqlSessionFactory(ZConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public ZSqlSession openSession() {
        return openSessionFromDataSource(configuration.getDefaultExecutorType(), null, false);
    }
    @Override
    public ZSqlSession openSession(boolean autoCommit) {
        return openSessionFromDataSource(configuration.getDefaultExecutorType(), null, autoCommit);
    }
    @Override
    public ZSqlSession openSession(ZExecutorType execType) {
        return openSessionFromDataSource(execType, null, false);
    }
    @Override
    public ZSqlSession openSession(TransactionIsolationLevel level) {
        return openSessionFromDataSource(configuration.getDefaultExecutorType(), level, false);
    }

    private ZSqlSession openSessionFromDataSource(ZExecutorType execType, TransactionIsolationLevel level, boolean autoCommit) {
        Transaction tx;

        // 获取环境属性，配置了连接数据库的参数
        final ZEnvironment environment = configuration.getEnvironment();
        // 获取事务工厂
        final TransactionFactory transactionFactory = getTransactionFactoryFromEnvironment(environment);
        // 创建事务
        tx = transactionFactory.newTransaction(environment.getDataSource(), level, autoCommit);
        final ZExecutor executor = configuration.newExecutor(execType, tx);
        return new ZDefaultSqlSession(configuration, executor, autoCommit);
    }

    private TransactionFactory getTransactionFactoryFromEnvironment(ZEnvironment environment) {
        if (environment == null || environment.getTransactionFactory() == null) {
            return new ManagedTransactionFactory();
        }
        return environment.getTransactionFactory();
    }


}
