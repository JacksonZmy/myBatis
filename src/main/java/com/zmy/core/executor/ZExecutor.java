package com.zmy.core.executor;

import com.zmy.core.mapping.ZBoundSql;
import com.zmy.core.mapping.ZMappedStatement;
import com.zmy.core.session.ZResultHandler;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.transaction.Transaction;

import java.sql.SQLException;
import java.util.List;

public interface ZExecutor {

    ResultHandler NO_RESULT_HANDLER = null;

    <T> List<T> query(ZMappedStatement ms, Object parameter, RowBounds rowBounds,
                      ZResultHandler resultHandler) throws SQLException;

    <T> List<T> query(ZMappedStatement ms, Object parameter, RowBounds rowBounds,
                      ZResultHandler resultHandler, CacheKey key, ZBoundSql boundSql) throws SQLException;

    int update(ZMappedStatement ms, Object parameter) throws SQLException;

    // 获取事务对象
    Transaction getTransaction();

    void commit(boolean required) throws SQLException;

    void rollback(boolean required) throws SQLException;

    // 关闭 Executor
    void close(boolean forceRollback);

    // 是否关闭
    boolean isClosed();

    // 清空一级缓存
    void clearLocalCache();

    // 是否缓存
    boolean isCached(ZMappedStatement ms, CacheKey key);

    // 创建缓存中用到的key
    CacheKey createCacheKey(ZMappedStatement ms, Object parameterObject, RowBounds rowBounds, ZBoundSql boundSql);

    void setExecutorWrapper(ZExecutor executor);

}
