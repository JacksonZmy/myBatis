package com.zmy.core.executor.statement;

import com.zmy.core.executor.ZExecutor;
import com.zmy.core.mapping.ZBoundSql;
import com.zmy.core.mapping.ZMappedStatement;
import com.zmy.core.session.ZResultHandler;
import org.apache.ibatis.mapping.ResultSetType;
import org.apache.ibatis.session.RowBounds;

import java.sql.*;
import java.util.List;

/**
 * @author Clinton Begin
 */
public class ZPreparedStatementHandler extends ZBaseStatementHandler {

  public ZPreparedStatementHandler(ZExecutor executor, ZMappedStatement mappedStatement, Object parameter, RowBounds rowBounds, ZResultHandler resultHandler, ZBoundSql boundSql) {
    super(executor, mappedStatement, parameter, rowBounds, resultHandler, boundSql);
  }

//  @Override
//  public int update(Statement statement) throws SQLException {
//    PreparedStatement ps = (PreparedStatement) statement;
//    ps.execute();
//    int rows = ps.getUpdateCount();
//    Object parameterObject = boundSql.getParameterObject();
//    KeyGenerator keyGenerator = mappedStatement.getKeyGenerator();
//    keyGenerator.processAfter(executor, mappedStatement, ps, parameterObject);
//    return rows;
//  }

//  @Override
//  public void batch(Statement statement) throws SQLException {
//    PreparedStatement ps = (PreparedStatement) statement;
//    ps.addBatch();
//  }

  @Override
  public <E> List<E> query(Statement statement, ZResultHandler resultHandler) throws SQLException {
    PreparedStatement ps = (PreparedStatement) statement;
    // 到了JDBC的流程
    ps.execute();
    // 处理结果集
    return resultSetHandler.handleResultSets(ps);
  }

  @Override
  public void parameterize(Statement statement) throws SQLException {
    parameterHandler.setParameters((PreparedStatement) statement);
  }

  @Override
  protected Statement instantiateStatement(Connection connection) throws SQLException {
    String sql = boundSql.getSql();
    if (false) {
//      String[] keyColumnNames = mappedStatement.getKeyColumns();
//      if (keyColumnNames == null) {
        return connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
//      } else {
        // 在执行 prepareStatement 方法的时候会进入进入到ConnectionLogger的invoker方法中
//        return connection.prepareStatement(sql, keyColumnNames);
//      }
    } else if (mappedStatement.getResultSetType() == ResultSetType.DEFAULT) {
      return connection.prepareStatement(sql);
    } else {
      return connection.prepareStatement(sql, mappedStatement.getResultSetType().getValue(), ResultSet.CONCUR_READ_ONLY);
    }
  }

}
