package com.zmy.core.executor;

import com.zmy.core.mapping.ZBoundSql;
import com.zmy.core.session.ZConfiguration;
import com.zmy.core.mapping.ZEnvironment;
import com.zmy.core.mapping.ZMappedStatement;
import com.zmy.core.session.ZResultHandler;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.ExecutorException;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.transaction.Transaction;

import javax.sql.DataSource;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * 执行DML和查询操作（操作statement和参数）
 * 提交和回滚
 */
public abstract class ZBaseExecutor implements ZExecutor{
    protected Transaction transaction;
    protected ZConfiguration configuration;
    private boolean closed;

    protected ZExecutor wrapper;

    protected ZBaseExecutor(ZConfiguration configuration, Transaction transaction) {
        this.configuration = configuration;
        this.transaction = transaction;
        this.closed = false;
        this.wrapper = this;
    }

    @Override
    public Transaction getTransaction() {
        if (closed) {
            throw new ExecutorException("Executor was closed.");
        }
        return transaction;
    }

    protected Connection getConnection() throws SQLException {
        Connection conn = transaction.getConnection();
        return conn;
    }

    @Override
    public void setExecutorWrapper(ZExecutor wrapper) {
        this.wrapper = wrapper;
    }

    @Override
    public boolean isClosed() {
        return closed;
    }
    // 关闭连接
    @Override
    public void close(boolean forceRollback) {
        try {
            // 回滚
        } catch (Exception e) {
            System.out.println(e);
        } finally {
            transaction = null;
//            deferredLoads = null;
//            localCache = null;
//            localOutputParameterCache = null;
            closed = true;
        }
    }


    @Override
    public void commit(boolean required) throws SQLException {
        if (closed) {
            throw new ExecutorException("Cannot commit, transaction is already closed");
        }
        // 清空一级缓存
//        clearLocalCache();
//        flushStatements(); // 啥都没做
        if (required) {
            transaction.commit();
        }
    }
    @Override
    public void rollback(boolean required) throws SQLException {
        if (!closed) {
            try {
//                clearLocalCache();
//                flushStatements(true);
            } finally {
                if (required) {
                    transaction.rollback();
                }
            }
        }
    }

    @Override
    public <E> List<E> query(ZMappedStatement ms, Object parameter, RowBounds rowBounds,
                             ZResultHandler resultHandler) throws SQLException {
        ZBoundSql boundSql = ms.getBoundSql(parameter);
        return query(ms, parameter, rowBounds, resultHandler, boundSql);
    }

    @Override
    public <T> List<T> query(ZMappedStatement ms, Object parameter, RowBounds rowBounds,
                             ZResultHandler resultHandler, ZBoundSql boundSql) throws SQLException {
        if (closed) {
            throw new ExecutorException("Executor 已经关闭。");
        }
        return queryFromDatabase(ms, parameter, rowBounds, resultHandler, boundSql);
    }

    @Override
    public int update(ZMappedStatement ms, Object parameter) throws SQLException {
        ErrorContext.instance().resource(ms.getResource()).activity("执行 update 操作").object(ms.getId());
        if (closed) {
            throw new ExecutorException("Executor 已经关闭。");
        }
//        clearLocalCache();
        return doUpdate(ms, parameter);
    }

    /**
     * TODO 处理缓存用
     * @param ms
     * @param parameter
     * @param rowBounds
     * @param resultHandler
     * @param boundSql
     * @return
     * @param <T>
     * @throws SQLException
     */
    private <T> List<T> queryFromDatabase(ZMappedStatement ms, Object parameter, RowBounds rowBounds, ZResultHandler resultHandler, ZBoundSql boundSql) throws SQLException {
        List<T> list = doQuery(ms, parameter, rowBounds, resultHandler, boundSql);
        return list;
    }

    protected abstract <T> List<T> doQuery(ZMappedStatement ms, Object parameter, RowBounds rowBounds, ZResultHandler resultHandler, ZBoundSql boundSql) throws SQLException;
    protected abstract int doUpdate(ZMappedStatement ms, Object parameter)
            throws SQLException;
}

