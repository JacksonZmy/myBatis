package com.zmy.core.executor;

import com.zmy.base.cache.ZTransactionalCacheManager;
import com.zmy.core.mapping.ZBoundSql;
import com.zmy.core.mapping.ZMappedStatement;
import com.zmy.core.mapping.ZParameterMapping;
import com.zmy.core.session.ZResultHandler;
import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.ExecutorException;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.transaction.Transaction;

import java.sql.SQLException;
import java.util.List;

public class ZCachingExecutor implements ZExecutor{

    private final ZExecutor delegate;
    // TODO
    private final ZTransactionalCacheManager tcm = new ZTransactionalCacheManager();

    public ZCachingExecutor(ZExecutor executor) {
        this.delegate = executor;
        delegate.setExecutorWrapper(this);
    }

    @Override
    public Transaction getTransaction() {
        return delegate.getTransaction();
    }

    @Override
    public <T> List<T> query(ZMappedStatement ms, Object parameter, RowBounds rowBounds, ZResultHandler resultHandler) throws SQLException {
        // 获取SQL
        ZBoundSql boundSql = ms.getBoundSql(parameter);
        // 创建CacheKey：什么样的SQL是同一条SQL？ >>
        CacheKey key = createCacheKey(ms, parameter, rowBounds, boundSql);
        return query(ms, parameter, rowBounds, resultHandler, key, boundSql);
    }

    @Override
    public <T> List<T> query(ZMappedStatement ms, Object parameter, RowBounds rowBounds, ZResultHandler resultHandler, CacheKey key, ZBoundSql boundSql) throws SQLException {
        Cache cache = ms.getCache();
        // cache 对象是在哪里创建的？  XMLMapperBuilder类 xmlconfigurationElement()
        // 由 <cache> 标签决定
        if (cache != null) {
            // flushCache="true" 清空一级二级缓存 >>
            flushCacheIfRequired(ms);
            if (ms.isUseCache() && resultHandler == null) {
                ensureNoOutParams(ms, boundSql);
                // 获取二级缓存
                // 缓存通过 TransactionalCacheManager、TransactionalCache 管理
                List<T> list = (List<T>) tcm.getObject(cache, key);
                if (list == null) {
                    list = delegate.query(ms, parameter, rowBounds, resultHandler, key, boundSql);
                    // 写入二级缓存
                    tcm.putObject(cache, key, list); // issue #578 and #116
                }
                return list;
            }
        }
        // 走到 SimpleExecutor | ReuseExecutor | BatchExecutor
        return delegate.query(ms, parameter, rowBounds, resultHandler, key, boundSql);
    }

    @Override
    public int update(ZMappedStatement ms, Object parameter) throws SQLException {
        flushCacheIfRequired(ms);
        return delegate.update(ms, parameter);
    }

    @Override
    public void commit(boolean required) throws SQLException {
        delegate.commit(required);
        tcm.commit();
    }

    @Override
    public void rollback(boolean required) throws SQLException {
        try {
            delegate.rollback(required);
        } finally {
            if (required) {
                tcm.rollback();
            }
        }
    }

    @Override
    public void close(boolean forceRollback) {
        try {
            if (forceRollback) {
                tcm.rollback();
            } else {
                tcm.commit();
            }
        } finally {
            delegate.close(forceRollback);
        }
    }

    @Override
    public boolean isClosed() {
        return delegate.isClosed();
    }

    @Override
    public void clearLocalCache() {
        delegate.clearLocalCache();
    }

    @Override
    public boolean isCached(ZMappedStatement ms, CacheKey key) {
        return delegate.isCached(ms, key);

    }

    @Override
    public CacheKey createCacheKey(ZMappedStatement ms, Object parameterObject, RowBounds rowBounds, ZBoundSql boundSql) {
        CacheKey cacheKey = delegate.createCacheKey(ms, parameterObject, rowBounds, boundSql);
        return cacheKey;
    }

    @Override
    public void setExecutorWrapper(ZExecutor executor) {

    }

    // 是否刷新二级缓存
    private void flushCacheIfRequired(ZMappedStatement ms) {
        Cache cache = ms.getCache();
        // 增删改查的标签上有属性：flushCache="true" （select语句默认是false）
        // 一级二级缓存都会被清理
        if (cache != null && ms.isFlushCacheRequired()) {
            tcm.clear(cache);
        }
    }

    /**
     * 确保没有返回参数发
     * @param ms
     * @param boundSql
     */
    private void ensureNoOutParams(ZMappedStatement ms, ZBoundSql boundSql) {
        if (ms.getStatementType() == StatementType.CALLABLE) {
            for (ZParameterMapping parameterMapping : boundSql.getParameterMappings()) {
                if (parameterMapping.getMode() != ParameterMode.IN) {
                    throw new ExecutorException("不支持使用 OUT 参数缓存存储过程。 请在 " + ms.getId());
                }
            }
        }
    }
}
