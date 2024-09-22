package com.zmy.core.executor;

import com.zmy.base.type.ZTypeHandlerRegistry;
import com.zmy.core.mapping.ZBoundSql;
import com.zmy.core.mapping.ZParameterMapping;
import com.zmy.core.session.ZConfiguration;
import com.zmy.core.mapping.ZEnvironment;
import com.zmy.core.mapping.ZMappedStatement;
import com.zmy.core.session.ZResultHandler;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.cache.impl.PerpetualCache;
import org.apache.ibatis.executor.BaseExecutor;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.ExecutorException;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.logging.jdbc.ConnectionLogger;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.LocalCacheScope;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.transaction.Transaction;
import org.apache.ibatis.type.TypeHandlerRegistry;

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

    private static final Log log = LogFactory.getLog(BaseExecutor.class);

    protected Transaction transaction;
    protected ZConfiguration configuration;
    // 本 executor 的状态
    private boolean closed;
    // 记录了SQL的层数，用于格式化输出SQL
    protected int queryStack;

    // 一级缓存初始化
    protected PerpetualCache localCache;
    protected PerpetualCache localOutputParameterCache;

    protected ZExecutor wrapper;

    protected ZBaseExecutor(ZConfiguration configuration, Transaction transaction) {
        this.configuration = configuration;
        this.transaction = transaction;
        this.closed = false;
        this.wrapper = this;
        this.localCache = new PerpetualCache("LocalCache");
        this.localOutputParameterCache = new PerpetualCache("LocalOutputParameterCache");
    }

    @Override
    public Transaction getTransaction() {
        if (closed) {
            throw new ExecutorException("Executor was closed.");
        }
        return transaction;
    }

    protected Connection getConnection(Log statementLog) throws SQLException {
        Connection connection = transaction.getConnection();
        if (statementLog.isDebugEnabled()) {
            // 创建Connection的日志代理对象
            return ConnectionLogger.newInstance(connection, statementLog, queryStack);
        } else {
            return connection;
        }
    }

    @Override
    public void setExecutorWrapper(ZExecutor wrapper) {
        this.wrapper = wrapper;
    }

    @Override
    public boolean isCached(ZMappedStatement ms, CacheKey key) {
        return localCache.getObject(key) != null;
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
            log.warn("关闭交易时出现意外异常。 原因: " + e);
        } finally {
            transaction = null;
//            deferredLoads = null;
            localCache = null;
            localOutputParameterCache = null;
            closed = true;
        }
    }

    @Override
    public void commit(boolean required) throws SQLException {
        if (closed) {
            throw new ExecutorException("Cannot commit, transaction is already closed");
        }
        // 清空一级缓存
        clearLocalCache();
//        flushStatements(); // 啥都没做
        if (required) {
            transaction.commit();
        }
    }
    @Override
    public void rollback(boolean required) throws SQLException {
        if (!closed) {
            try {
                clearLocalCache();
//                flushStatements(true);
            } finally {
                if (required) {
                    transaction.rollback();
                }
            }
        }
    }

    @Override
    public CacheKey createCacheKey(ZMappedStatement ms, Object parameterObject, RowBounds rowBounds, ZBoundSql boundSql) {
        if (closed) {
            throw new ExecutorException("Executor was closed.");
        }
        CacheKey cacheKey = new CacheKey();
        // -1381545870:4796102018:com.gupaoedu.mapper.BlogMapper.selectBlogById:0:2147483647:select * from blog where bid = ?:1:development
        cacheKey.update(ms.getId()); // com.gupaoedu.mapper.BlogMapper.selectBlogById
        cacheKey.update(rowBounds.getOffset()); // 0
        cacheKey.update(rowBounds.getLimit()); // 2147483647 = 2^31-1
        cacheKey.update(boundSql.getSql());
        List<ZParameterMapping> parameterMappings = boundSql.getParameterMappings();
        ZTypeHandlerRegistry typeHandlerRegistry = ms.getConfiguration().getTypeHandlerRegistry();
        // mimic DefaultParameterHandler logic
        for (ZParameterMapping parameterMapping : parameterMappings) {
            if (parameterMapping.getMode() != ParameterMode.OUT) {
                Object value;
                String propertyName = parameterMapping.getProperty();
                if (boundSql.hasAdditionalParameter(propertyName)) {
                    value = boundSql.getAdditionalParameter(propertyName);
                } else if (parameterObject == null) {
                    value = null;
                } else if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
                    value = parameterObject;
                } else {
                    MetaObject metaObject = configuration.newMetaObject(parameterObject);
                    value = metaObject.getValue(propertyName);
                }
                cacheKey.update(value); // development
            }
        }
        if (configuration.getEnvironment() != null) {
            // issue #176
            cacheKey.update(configuration.getEnvironment().getId());
        }
        return cacheKey;
    }

    /**
     * 清理一级缓存
     */
    @Override
    public void clearLocalCache() {
        if (!closed) {
            localCache.clear();
            localOutputParameterCache.clear();
        }
    }

    /**
     * 处理缓存
     * @param ms
     * @param parameter
     * @param rowBounds
     * @param resultHandler
     * @return
     * @param <E>
     * @throws SQLException
     */
    @Override
    public <E> List<E> query(ZMappedStatement ms, Object parameter, RowBounds rowBounds,
                             ZResultHandler resultHandler) throws SQLException {
        ZBoundSql boundSql = ms.getBoundSql(parameter);
        // 一级缓存和二级缓存的CacheKey是同一个
        CacheKey key = createCacheKey(ms, parameter, rowBounds, boundSql);
        return query(ms, parameter, rowBounds, resultHandler, key, boundSql);
    }

    @Override
    public <T> List<T> query(ZMappedStatement ms, Object parameter, RowBounds rowBounds,
                             ZResultHandler resultHandler, CacheKey key, ZBoundSql boundSql) throws SQLException {
        if (closed) {
            throw new ExecutorException("Executor 已经关闭。");
        }
        if (queryStack == 0 && ms.isFlushCacheRequired()) {
            // flushCache="true"时，即使是查询，也清空一级缓存
            clearLocalCache();
        }
        List<T> list;
        try {
            // 防止递归查询重复处理缓存
            queryStack++;
            // 查询一级缓存
            // ResultHandler 和 ResultSetHandler的区别
            list = resultHandler == null ? (List<T>) localCache.getObject(key) : null;
            if (list != null) {
                handleLocallyCachedOutputParameters(ms, key, parameter, boundSql);
            } else {
                // 真正的查询流程
                list = queryFromDatabase(ms, parameter, rowBounds, resultHandler, key, boundSql);
            }
        } finally {
            queryStack--;
        }
        if (queryStack == 0) {
//            for (BaseExecutor.DeferredLoad deferredLoad : deferredLoads) {
//                deferredLoad.load();
//            }
//            deferredLoads.clear();

            // statement 级别显示关闭一级缓存
            if (configuration.getLocalCacheScope() == LocalCacheScope.STATEMENT) {
                clearLocalCache();
            }
        }
        return list;
    }

    @Override
    public int update(ZMappedStatement ms, Object parameter) throws SQLException {
        ErrorContext.instance().resource(ms.getResource()).activity("执行 update 操作").object(ms.getId());
        if (closed) {
            throw new ExecutorException("Executor 已经关闭。");
        }
        // DML操作之前先清空一级缓存
        clearLocalCache();
        return doUpdate(ms, parameter);
    }

    /**
     * 处理本地缓存的输出参数
     * 存储过程用
     * @param ms
     * @param key
     * @param parameter
     * @param boundSql
     */
    private void handleLocallyCachedOutputParameters(ZMappedStatement ms, CacheKey key, Object parameter, ZBoundSql boundSql) {
        if (ms.getStatementType() == StatementType.CALLABLE) {
            final Object cachedParameter = localOutputParameterCache.getObject(key);
            if (cachedParameter != null && parameter != null) {
                final MetaObject metaCachedParameter = configuration.newMetaObject(cachedParameter);
                final MetaObject metaParameter = configuration.newMetaObject(parameter);
                for (ZParameterMapping parameterMapping : boundSql.getParameterMappings()) {
                    if (parameterMapping.getMode() != ParameterMode.IN) {
                        final String parameterName = parameterMapping.getProperty();
                        final Object cachedValue = metaCachedParameter.getValue(parameterName);
                        metaParameter.setValue(parameterName, cachedValue);
                    }
                }
            }
        }
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
    private <T> List<T> queryFromDatabase(ZMappedStatement ms, Object parameter, RowBounds rowBounds,
                                          ZResultHandler resultHandler, CacheKey key, ZBoundSql boundSql) throws SQLException {
        List<T> list = doQuery(ms, parameter, rowBounds, resultHandler, boundSql);
        localCache.putObject(key, list);
        // TODO 存储过程
        if (ms.getStatementType() == StatementType.CALLABLE) {
            localOutputParameterCache.putObject(key, parameter);
        }
        return list;
    }

    protected abstract <T> List<T> doQuery(ZMappedStatement ms, Object parameter, RowBounds rowBounds, ZResultHandler resultHandler, ZBoundSql boundSql) throws SQLException;
    protected abstract int doUpdate(ZMappedStatement ms, Object parameter)
            throws SQLException;
}

