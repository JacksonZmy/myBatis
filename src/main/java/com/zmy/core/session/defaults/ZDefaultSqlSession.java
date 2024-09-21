package com.zmy.core.session.defaults;

import com.zmy.core.session.ZConfiguration;
import com.zmy.core.executor.ZExecutor;
import com.zmy.core.mapping.ZMappedStatement;
import com.zmy.core.session.ZResultHandler;
import com.zmy.core.session.ZSqlSession;
import org.apache.ibatis.exceptions.ExceptionFactory;
import org.apache.ibatis.exceptions.TooManyResultsException;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.RowBounds;

import java.sql.SQLException;
import java.util.List;

public class ZDefaultSqlSession implements ZSqlSession {

    private ZConfiguration configuration;
    private ZExecutor executor;

    private final boolean autoCommit;
    private boolean dirty;

    public ZDefaultSqlSession(ZConfiguration configuration, ZExecutor excutor, boolean autoCommit){
        this.configuration = configuration;
        this.executor = excutor;
        this.dirty = false;
        this.autoCommit = autoCommit;
    }

    /**
     * @param statement sql语句
     * @param parameter sql参数
     * @param <T>
     * @return
     */
    public <T> T selectOne(String statement, Object parameter){
        List<T> list = this.selectList(statement, parameter);
        if (list.size() == 1) {
            return list.get(0);
        } else if (list.size() > 1) {
            throw new TooManyResultsException("Expected one result (or null) to be returned by selectOne(), but found: " + list.size());
        } else {
            return null;
        }
    }

    @Override
    public <E> List<E> selectList(String statement) {
        // 为了提供多种重载（简化方法使用），和默认值
        // 让参数少的调用参数多的方法，只实现一次
        return selectList(statement, null);
    }

    @Override
    public <T> List<T> selectList(String statement, Object parameter) {
        return selectList(statement, parameter, RowBounds.DEFAULT);
    }

    @Override
    public <T> List<T> selectList(String statement, Object parameter, RowBounds rowBounds) {
        try {
            // TODO
            ZMappedStatement ms = configuration.getMappedStatement(statement);
            // 如果 cacheEnabled = true（默认），Executor会被 CachingExecutor装饰
            return executor.query(ms, parameter, rowBounds, null);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ErrorContext.instance().reset();
        }
        return null;
    }

    @Override
    public void select(String statement, ZResultHandler handler) {
        select(statement, null, RowBounds.DEFAULT, handler);
    }

    @Override
    public void select(String statement, Object parameter, ZResultHandler handler) {
        select(statement, parameter, RowBounds.DEFAULT, handler);
    }

    @Override
    public void select(String statement, Object parameter, RowBounds rowBounds, ZResultHandler handler) {
        try {
            ZMappedStatement ms = configuration.getMappedStatement(statement);
//            executor.query(ms, wrapCollection(parameter), rowBounds, handler);
            executor.query(ms, parameter, rowBounds, handler);
        } catch (Exception e) {
            throw ExceptionFactory.wrapException("查询数据库错误，原因: " + e, e);
        } finally {
            ErrorContext.instance().reset();
        }
    }

    @Override
    public int insert(String statement) {
        return insert(statement, null);
    }

    @Override
    public int insert(String statement, Object parameter) {
        return update(statement, parameter);
    }

    @Override
    public int update(String statement) {
        return update(statement, null);
    }

    @Override
    public int update(String statement, Object parameter) {
        try {
            dirty = true;
            ZMappedStatement ms = configuration.getMappedStatement(statement);
            return executor.update(ms, parameter);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            ErrorContext.instance().reset();
        }
    }

    @Override
    public int delete(String statement) {
        return delete(statement, null);
    }

    @Override
    public int delete(String statement, Object parameter) {
        return update(statement, parameter);
    }

    @Override
    public <T> T getMapper(Class<T> type) {
        return configuration.getMapper(type, this);
    }

    @Override
    public void commit() {
        commit(false);
    }

    @Override
    public void commit(boolean force) {
        try {
            executor.commit(isCommitOrRollbackRequired(force));
            dirty = false;
        } catch (Exception e) {
            throw ExceptionFactory.wrapException("Error committing transaction.  Cause: " + e, e);
        } finally {
            ErrorContext.instance().reset();
        }
    }

    @Override
    public void rollback() {
        rollback(false);
    }

    @Override
    public void rollback(boolean force) {
        try {
            executor.rollback(isCommitOrRollbackRequired(force));
            dirty = false;
        } catch (Exception e) {
            throw ExceptionFactory.wrapException("Error rolling back transaction.  Cause: " + e, e);
        } finally {
            ErrorContext.instance().reset();
        }
    }

    private boolean isCommitOrRollbackRequired(boolean force) {
        // 如果 force 为 true，或者 autoCommit = false && dirty = true
        // 返回 true
        return (!autoCommit && dirty) || force;
    }

    /**
     * TODO
     */
    @Override
    public void close() {
        executor.close(true);
        dirty = false;
    }

    @Override
    public ZConfiguration getConfiguration() {
        return configuration;
    }
}
