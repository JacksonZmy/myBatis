package com.zmy.core.executor;

import com.zmy.core.mapping.ZBoundSql;
import com.zmy.core.mapping.ZMappedStatement;
import com.zmy.core.session.ZResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.transaction.Transaction;

import java.sql.SQLException;
import java.util.List;

public interface ZExecutor {

    public <T> List<T> query(ZMappedStatement ms, Object parameter, RowBounds rowBounds, ZResultHandler resultHandler) throws SQLException;

    public <T> List<T> query(ZMappedStatement ms, Object parameter, RowBounds rowBounds, ZResultHandler resultHandler,
                             ZBoundSql boundSql) throws SQLException;

    // 获取事务对象
    Transaction getTransaction();

    void commit(boolean required) throws SQLException;

    void rollback(boolean required) throws SQLException;

    // 关闭 Executor
    void close(boolean forceRollback);

    // 是否关闭
    boolean isClosed();

    void setExecutorWrapper(ZExecutor executor);

}
