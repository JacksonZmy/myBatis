package com.zmy.base.transaction.jdbc;

import com.zmy.base.transaction.ZTransaction;
import com.zmy.base.transaction.ZTransactionFactory;
import org.apache.ibatis.session.TransactionIsolationLevel;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * Creates {@link org.apache.ibatis.transaction.jdbc.JdbcTransaction} instances.
 *
 * @author Clinton Begin
 *
 * @see org.apache.ibatis.transaction.jdbc.JdbcTransaction
 */
public class ZJdbcTransactionFactory implements ZTransactionFactory {

  @Override
  public ZTransaction newTransaction(Connection conn) {
    return new ZJdbcTransaction(conn);
  }

  @Override
  public ZTransaction newTransaction(DataSource ds, TransactionIsolationLevel level, boolean autoCommit) {
    return new ZJdbcTransaction(ds, level, autoCommit);
  }
}
