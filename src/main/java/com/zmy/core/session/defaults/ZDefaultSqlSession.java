package com.zmy.core.session.defaults;

import com.zmy.core.session.ZConfiguration;
import com.zmy.core.executor.ZExecutor;
import com.zmy.core.mapping.ZMappedStatement;
import com.zmy.core.session.ZSqlSession;
import org.apache.ibatis.exceptions.TooManyResultsException;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.session.RowBounds;

import java.util.List;

public class ZDefaultSqlSession implements ZSqlSession {

    private ZConfiguration configuration;
    private ZExecutor executor;

    public ZDefaultSqlSession(ZConfiguration configuration, ZExecutor excutor){
        this.configuration = configuration;
        this.executor = excutor;
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
    public <T> List<T> selectList(String statement, Object parameter) {
        // 为了提供多种重载（简化方法使用），和默认值
        // 让参数少的调用参数多的方法，只实现一次
        return this.selectList(statement, parameter, RowBounds.DEFAULT);
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




    /**
     * TODO
     */
    @Override
    public void close() {
        executor.close(true);
    }
}
