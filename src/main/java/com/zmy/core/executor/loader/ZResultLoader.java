package com.zmy.core.executor.loader;

import com.zmy.core.executor.ZExecutor;
import com.zmy.core.executor.ZResultExtractor;
import com.zmy.core.mapping.ZBoundSql;
import com.zmy.core.mapping.ZEnvironment;
import com.zmy.core.mapping.ZMappedStatement;
import com.zmy.core.session.ZConfiguration;
import com.zmy.core.session.ZExecutorType;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.ExecutorException;
import org.apache.ibatis.executor.ResultExtractor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.factory.DefaultObjectFactory;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.transaction.Transaction;
import org.apache.ibatis.transaction.TransactionFactory;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;

/**
 * @author Clinton Begin
 */
public class ZResultLoader {

  protected final ZConfiguration configuration;
  protected final ZExecutor executor;
  protected final ZMappedStatement mappedStatement;
  protected final Object parameterObject;
  protected final Class<?> targetType;
  protected final ObjectFactory objectFactory;
//  protected final CacheKey cacheKey;
  protected final ZBoundSql boundSql;
  protected final ZResultExtractor resultExtractor;
  protected final long creatorThreadId;

  protected boolean loaded;
  protected Object resultObject;

  public ZResultLoader(ZConfiguration config, ZExecutor executor, ZMappedStatement mappedStatement, Object parameterObject, Class<?> targetType, ZBoundSql boundSql) {
    this.configuration = config;
    this.executor = executor;
    this.mappedStatement = mappedStatement;
    this.parameterObject = parameterObject;
    this.targetType = targetType;
    this.objectFactory = new DefaultObjectFactory();
//    this.cacheKey = cacheKey;
    this.boundSql = boundSql;
    this.resultExtractor = new ZResultExtractor(configuration, objectFactory);
    this.creatorThreadId = Thread.currentThread().getId();
  }

  public Object loadResult() throws SQLException {
    List<Object> list = selectList();
    resultObject = resultExtractor.extractObjectFromList(list, targetType);
    return resultObject;
  }

  private <E> List<E> selectList() throws SQLException {
    ZExecutor localExecutor = executor;
    if (Thread.currentThread().getId() != this.creatorThreadId || localExecutor.isClosed()) {
      localExecutor = newExecutor();
    }
    try {
      return localExecutor.query(mappedStatement, parameterObject, RowBounds.DEFAULT, null, boundSql);
    } finally {
      if (localExecutor != executor) {
        localExecutor.close(false);
      }
    }
  }

  private ZExecutor newExecutor() {
    final ZEnvironment environment = configuration.getEnvironment();
    if (environment == null) {
      throw new ExecutorException("ResultLoader could not load lazily.  Environment was not configured.");
    }
    final DataSource ds = environment.getDataSource();
    if (ds == null) {
      throw new ExecutorException("ResultLoader could not load lazily.  DataSource was not configured.");
    }
//    final TransactionFactory transactionFactory = environment.getTransactionFactory();
//    final Transaction tx = transactionFactory.newTransaction(ds, null, false);
    return configuration.newExecutor(ZExecutorType.SIMPLE);
  }

  public boolean wasNull() {
    return resultObject == null;
  }

}
