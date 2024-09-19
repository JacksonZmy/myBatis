package com.zmy.core.session;

import com.zmy.base.binding.ZMapperRegistry;
import com.zmy.base.scripting.ZLanguageDriver;
import com.zmy.base.scripting.ZLanguageDriverRegistry;
import com.zmy.base.scripting.defaults.ZRawLanguageDriver;
import com.zmy.base.scripting.xmltags.ZXMLLanguageDriver;
import com.zmy.base.type.ZTypeHandlerRegistry;
import com.zmy.core.executor.ZExecutor;
import com.zmy.core.executor.ZSimpleExecutor;
import com.zmy.core.executor.parameter.ZParameterHandler;
import com.zmy.core.executor.resultset.ZDefaultResultSetHandler;
import com.zmy.core.executor.resultset.ZResultSetHandler;
import com.zmy.core.executor.statement.ZRoutingStatementHandler;
import com.zmy.core.executor.statement.ZStatementHandler;
import com.zmy.core.mapping.*;
import com.zmy.core.session.ZExecutorType;
import org.apache.ibatis.binding.MapperRegistry;
import org.apache.ibatis.cache.decorators.FifoCache;
import org.apache.ibatis.cache.decorators.LruCache;
import org.apache.ibatis.cache.decorators.SoftCache;
import org.apache.ibatis.cache.decorators.WeakCache;
import org.apache.ibatis.cache.impl.PerpetualCache;
import org.apache.ibatis.datasource.jndi.JndiDataSourceFactory;
import org.apache.ibatis.datasource.pooled.PooledDataSourceFactory;
import org.apache.ibatis.datasource.unpooled.UnpooledDataSourceFactory;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.loader.cglib.CglibProxyFactory;
import org.apache.ibatis.executor.loader.javassist.JavassistProxyFactory;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.resultset.DefaultResultSetHandler;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.logging.commons.JakartaCommonsLoggingImpl;
import org.apache.ibatis.logging.jdk14.Jdk14LoggingImpl;
import org.apache.ibatis.logging.log4j.Log4jImpl;
import org.apache.ibatis.logging.log4j2.Log4j2Impl;
import org.apache.ibatis.logging.nologging.NoLoggingImpl;
import org.apache.ibatis.logging.slf4j.Slf4jImpl;
import org.apache.ibatis.logging.stdout.StdOutImpl;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.parsing.XNode;
import org.apache.ibatis.plugin.InterceptorChain;
import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.ReflectorFactory;
import org.apache.ibatis.reflection.factory.DefaultObjectFactory;
import org.apache.ibatis.reflection.wrapper.DefaultObjectWrapperFactory;
import org.apache.ibatis.scripting.LanguageDriver;
import org.apache.ibatis.scripting.LanguageDriverRegistry;
import org.apache.ibatis.scripting.defaults.RawLanguageDriver;
import org.apache.ibatis.scripting.xmltags.XMLLanguageDriver;
import org.apache.ibatis.session.*;
import org.apache.ibatis.transaction.Transaction;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.apache.ibatis.transaction.managed.ManagedTransactionFactory;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeAliasRegistry;
import org.apache.ibatis.type.TypeHandlerRegistry;

import java.util.*;

public class ZConfiguration {

    protected String databaseId;
    public String getDatabaseId() {
        return databaseId;
    }
    public void setDatabaseId(String databaseId) {
        this.databaseId = databaseId;
    }

    public ZConfiguration(ZEnvironment environment) {
        this();
        this.environment = environment;
    }

    // 默认 executor 的类型（Simple）
    protected ZExecutorType defaultExecutorType = ZExecutorType.SIMPLE;

    // TODO 不知道什么用
    protected final Map<String, XNode> sqlFragments = new HashMap<>();
    public Map<String, XNode> getSqlFragments() {
        return sqlFragments;
    }


    protected final ZTypeHandlerRegistry typeHandlerRegistry = new ZTypeHandlerRegistry(this);
    public ZTypeHandlerRegistry getTypeHandlerRegistry() {
        return typeHandlerRegistry;
    }

    // 别名的注册器
    protected final TypeAliasRegistry typeAliasRegistry = new TypeAliasRegistry();
    public TypeAliasRegistry getTypeAliasRegistry() {
        return typeAliasRegistry;
    }


    // 维护接口方法与SQL关系
    protected final Map<String, ZMappedStatement> mappedStatements = new HashMap<>();
    public void addMappedStatement(ZMappedStatement ms) {
        mappedStatements.put(ms.getId(), ms);
    }
    public ZMappedStatement getMappedStatement(String interfaceName) {
        return this.mappedStatements.get(interfaceName);
    }
    public Collection<String> getMappedStatementNames() {
        return mappedStatements.keySet();
    }
    public Collection<ZMappedStatement> getMappedStatements() {
        return mappedStatements.values();
    }
    public boolean hasStatement(String statementName, boolean validateIncompleteStatements) {
        return mappedStatements.containsKey(statementName);
    }

    protected ZEnvironment environment;
    public ZEnvironment getEnvironment() {
        return environment;
    }
    public void setEnvironment(ZEnvironment environment) {
        this.environment = environment;
    }

    // 维护接口与工厂类关系
    protected final ZMapperRegistry mapperRegistry = new ZMapperRegistry(this);
    public void addMappers(String packageName) {
        mapperRegistry.addMappers(packageName);
    }
    public <T> void addMapper(Class<T> type) {
        mapperRegistry.addMapper(type);
    }
    public <T> T getMapper(Class<T> type, SqlSession sqlSession) {
        // mapperRegistry中注册的有Mapper的相关信息 在解析映射文件时 调用过addMapper方法
        return mapperRegistry.getMapper(type, sqlSession);
    }
    public boolean hasMapper(Class<?> type) {
        return mapperRegistry.hasMapper(type);
    }

    protected Properties variables = new Properties();
    public Properties getVariables() {
        return variables;
    }
    public void setVariables(Properties variables) {
        this.variables = variables;
    }

    protected Class<? extends Log> logImpl;
    public Class<? extends Log> getLogImpl() {
        return logImpl;
    }
    public void setLogImpl(Class<? extends Log> logImpl) {
        if (logImpl != null) {
            this.logImpl = logImpl; // 记录日志的类型
            // TODO 设置 适配选择
//            LogFactory.useCustomLogging(this.logImpl);
        }
    }

    protected final ZLanguageDriverRegistry languageRegistry = new ZLanguageDriverRegistry();
    public ZLanguageDriver getDefaultScriptingLanguageInstance() {
        return languageRegistry.getDefaultDriver();
    }
    public ZLanguageDriver getLanguageDriver(Class<? extends ZLanguageDriver> langClass) {
        if (langClass == null) {
            return languageRegistry.getDefaultDriver();
        }
        languageRegistry.register(langClass);
        return languageRegistry.getDriver(langClass);
    }

    public MetaObject newMetaObject(Object object) {
//        return MetaObject.forObject(object, objectFactory, objectWrapperFactory, reflectorFactory);
        return MetaObject.forObject(object, new DefaultObjectFactory(), new DefaultObjectWrapperFactory(), reflectorFactory);
    }

    protected ReflectorFactory reflectorFactory = new DefaultReflectorFactory();
    public ReflectorFactory getReflectorFactory() {
        return reflectorFactory;
    }
    public void setReflectorFactory(ReflectorFactory reflectorFactory) {
        this.reflectorFactory = reflectorFactory;
    }

    protected final Map<String, ZResultMap> resultMaps = new HashMap<>();
    public Collection<String> getResultMapNames() {
        return resultMaps.keySet();
    }

    public Collection<ZResultMap> getResultMaps() {
        return resultMaps.values();
    }
    public ZResultMap getResultMap(String id) {
        return resultMaps.get(id);
    }
    public boolean hasResultMap(String id) {
        return resultMaps.containsKey(id);
    }
    public void addResultMap(ZResultMap rm) {
        resultMaps.put(rm.getId(), rm);
        checkLocallyForDiscriminatedNestedResultMaps(rm);
        checkGloballyForDiscriminatedNestedResultMaps(rm);
    }
    // Slow but a one time cost. A better solution is welcome.
    protected void checkLocallyForDiscriminatedNestedResultMaps(ZResultMap rm) {
        if (!rm.hasNestedResultMaps() && rm.getDiscriminator() != null) {
            for (Map.Entry<String, String> entry : rm.getDiscriminator().getDiscriminatorMap().entrySet()) {
                String discriminatedResultMapName = entry.getValue();
                if (hasResultMap(discriminatedResultMapName)) {
                    ZResultMap discriminatedResultMap = resultMaps.get(discriminatedResultMapName);
                    if (discriminatedResultMap.hasNestedResultMaps()) {
                        rm.forceNestedResultMaps();
                        break;
                    }
                }
            }
        }
    }
    // Slow but a one time cost. A better solution is welcome.
    protected void checkGloballyForDiscriminatedNestedResultMaps(ZResultMap rm) {
        if (rm.hasNestedResultMaps()) {
            for (Map.Entry<String, ZResultMap> entry : resultMaps.entrySet()) {
                Object value = entry.getValue();
                if (value instanceof ResultMap) {
                    ResultMap entryResultMap = (ResultMap) value;
                    if (!entryResultMap.hasNestedResultMaps() && entryResultMap.getDiscriminator() != null) {
                        Collection<String> discriminatedResultMapNames = entryResultMap.getDiscriminator().getDiscriminatorMap().values();
                        if (discriminatedResultMapNames.contains(rm.getId())) {
                            entryResultMap.forceNestedResultMaps();
                        }
                    }
                }
            }
        }
    }


    protected final Map<String, ZParameterMap> parameterMaps = new HashMap<>();
    public boolean hasParameterMap(String id) {
        return parameterMaps.containsKey(id);
    }
    public Collection<String> getParameterMapNames() {
        return parameterMaps.keySet();
    }
    public Collection<ZParameterMap> getParameterMaps() {
        return parameterMaps.values();
    }
    public ZParameterMap getParameterMap(String id) {
        return parameterMaps.get(id);
    }
    public void addParameterMap(ZParameterMap pm) {
        parameterMaps.put(pm.getId(), pm);
    }

    public ZParameterHandler newParameterHandler(ZMappedStatement mappedStatement, Object parameterObject, ZBoundSql boundSql) {
        ZParameterHandler parameterHandler = mappedStatement.getLang().createParameterHandler(mappedStatement, parameterObject, boundSql);
        // 植入插件逻辑（返回代理对象）
//        parameterHandler = (ParameterHandler) interceptorChain.pluginAll(parameterHandler);
        return parameterHandler;
    }
    public ZResultSetHandler newResultSetHandler(ZExecutor executor, ZMappedStatement mappedStatement, RowBounds rowBounds, ZParameterHandler parameterHandler,
                                                 ZResultHandler resultHandler, ZBoundSql boundSql) {
        ZResultSetHandler resultSetHandler = new ZDefaultResultSetHandler(executor, mappedStatement, parameterHandler, resultHandler, boundSql, rowBounds);
        // 植入插件逻辑（返回代理对象）
//        resultSetHandler = (ResultSetHandler) interceptorChain.pluginAll(resultSetHandler);
        return resultSetHandler;
    }
    public ZStatementHandler newStatementHandler(ZExecutor executor, ZMappedStatement mappedStatement, RowBounds rowBounds, Object parameter,
                                                 ZResultHandler resultHandler, ZBoundSql boundSql) {
        ZStatementHandler statementHandler = new ZRoutingStatementHandler(executor, mappedStatement, parameter, rowBounds, resultHandler, boundSql);
        return statementHandler;
    }

    public ZExecutor newExecutor(ZExecutorType executorType) {
        return newExecutor(executorType, null);
    }
    /**
     * TODO
     * @param executorType
     * @return
     */
    public ZExecutor newExecutor(ZExecutorType executorType, Transaction transaction) {
        executorType = executorType == null ? defaultExecutorType : executorType;
        executorType = executorType == null ? ZExecutorType.SIMPLE : executorType;
        ZExecutor executor;
        if (ZExecutorType.BATCH == executorType) {
//            executor = new ZBatchExecutor(this);
        } else if (ZExecutorType.REUSE == executorType) {
//            executor = new ZReuseExecutor(this);
        } else {
            // 默认 SimpleExecutor
//            executor = new ZSimpleExecutor(this, environment);
        }
        // 二级缓存开关，settings 中的 cacheEnabled 默认是 true
//        if (cacheEnabled) {
//            executor = new CachingExecutor(executor);
//        }
        // 植入插件的逻辑，至此，四大对象已经全部拦截完毕
//        executor = (Executor) interceptorChain.pluginAll(executor);
        executor = new ZSimpleExecutor(this, transaction);
        return executor;
    }

    public ZConfiguration() {
        this.defaultExecutorType = ZExecutorType.SIMPLE;
        this.reflectorFactory = new DefaultReflectorFactory();
        this.typeAliasRegistry.registerAlias("JDBC", JdbcTransactionFactory.class);
        this.typeAliasRegistry.registerAlias("MANAGED", ManagedTransactionFactory.class);
        this.typeAliasRegistry.registerAlias("JNDI", JndiDataSourceFactory.class);
        this.typeAliasRegistry.registerAlias("POOLED", PooledDataSourceFactory.class);
        this.typeAliasRegistry.registerAlias("UNPOOLED", UnpooledDataSourceFactory.class);
        this.typeAliasRegistry.registerAlias("PERPETUAL", PerpetualCache.class);
        this.typeAliasRegistry.registerAlias("FIFO", FifoCache.class);
        this.typeAliasRegistry.registerAlias("LRU", LruCache.class);
        this.typeAliasRegistry.registerAlias("SOFT", SoftCache.class);
        this.typeAliasRegistry.registerAlias("WEAK", WeakCache.class);
        this.typeAliasRegistry.registerAlias("DB_VENDOR", VendorDatabaseIdProvider.class);
        this.typeAliasRegistry.registerAlias("XML", XMLLanguageDriver.class);
        this.typeAliasRegistry.registerAlias("RAW", RawLanguageDriver.class);
        this.typeAliasRegistry.registerAlias("SLF4J", Slf4jImpl.class);
        this.typeAliasRegistry.registerAlias("COMMONS_LOGGING", JakartaCommonsLoggingImpl.class);
        this.typeAliasRegistry.registerAlias("LOG4J", Log4jImpl.class);
        this.typeAliasRegistry.registerAlias("LOG4J2", Log4j2Impl.class);
        this.typeAliasRegistry.registerAlias("JDK_LOGGING", Jdk14LoggingImpl.class);
        this.typeAliasRegistry.registerAlias("STDOUT_LOGGING", StdOutImpl.class);
        this.typeAliasRegistry.registerAlias("NO_LOGGING", NoLoggingImpl.class);
        this.typeAliasRegistry.registerAlias("CGLIB", CglibProxyFactory.class);
        this.typeAliasRegistry.registerAlias("JAVASSIST", JavassistProxyFactory.class);

        languageRegistry.setDefaultDriverClass(ZXMLLanguageDriver.class);
        languageRegistry.register(ZRawLanguageDriver.class);
    }


    protected boolean returnInstanceForEmptyRow;
    public boolean isReturnInstanceForEmptyRow() {
        return returnInstanceForEmptyRow;
    }
    public void setReturnInstanceForEmptyRow(boolean returnEmptyInstance) {
        this.returnInstanceForEmptyRow = returnEmptyInstance;
    }

    protected boolean useColumnLabel = true;
    public void setUseColumnLabel(boolean useColumnLabel) {
        this.useColumnLabel = useColumnLabel;
    }
    public boolean isUseColumnLabel() {
        return useColumnLabel;
    }

    protected boolean mapUnderscoreToCamelCase;
    public boolean isMapUnderscoreToCamelCase() {
        return mapUnderscoreToCamelCase;
    }
    public void setMapUnderscoreToCamelCase(boolean mapUnderscoreToCamelCase) {
        this.mapUnderscoreToCamelCase = mapUnderscoreToCamelCase;
    }

    protected ZAutoMappingUnknownColumnBehavior autoMappingUnknownColumnBehavior = ZAutoMappingUnknownColumnBehavior.NONE;
    public ZAutoMappingUnknownColumnBehavior getAutoMappingUnknownColumnBehavior() {
        return autoMappingUnknownColumnBehavior;
    }
    public void setAutoMappingUnknownColumnBehavior(ZAutoMappingUnknownColumnBehavior autoMappingUnknownColumnBehavior) {
        this.autoMappingUnknownColumnBehavior = autoMappingUnknownColumnBehavior;
    }

    protected JdbcType jdbcTypeForNull = JdbcType.OTHER;
    public void setJdbcTypeForNull(JdbcType jdbcTypeForNull) {
        this.jdbcTypeForNull = jdbcTypeForNull;
    }
    public JdbcType getJdbcTypeForNull() {
        return jdbcTypeForNull;
    }


}
