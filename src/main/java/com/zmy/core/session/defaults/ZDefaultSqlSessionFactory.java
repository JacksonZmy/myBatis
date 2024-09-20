package com.zmy.core.session.defaults;

import com.zmy.core.session.ZConfiguration;
import com.zmy.core.executor.ZExecutor;
import com.zmy.core.mapping.ZEnvironment;
import com.zmy.core.session.ZExecutorType;
import com.zmy.core.session.ZSqlSession;
import com.zmy.core.session.ZSqlSessionFactory;
import org.apache.ibatis.mapping.Environment;
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
        return openSessionFromDataSource();
    }

    private ZSqlSession openSessionFromDataSource() {
        return openSessionFromDataSource(ZExecutorType.SIMPLE, null);
    }

    private ZSqlSession openSessionFromDataSource(ZExecutorType execType, TransactionIsolationLevel level) {
        Transaction tx;

        // 获取环境属性，配置了连接数据库的参数
        final ZEnvironment environment = configuration.getEnvironment();
        // 获取事务工厂
        final TransactionFactory transactionFactory = getTransactionFactoryFromEnvironment(environment);
        // 创建事务
        tx = transactionFactory.newTransaction(environment.getDataSource(), level, false);
        final ZExecutor executor = configuration.newExecutor(execType, tx);
        return new ZDefaultSqlSession(configuration, executor);
    }

    private TransactionFactory getTransactionFactoryFromEnvironment(ZEnvironment environment) {
        if (environment == null || environment.getTransactionFactory() == null) {
            return new ManagedTransactionFactory();
        }
        return environment.getTransactionFactory();
    }


}
