package com.zmy.base.transaction;

import org.apache.ibatis.session.TransactionIsolationLevel;
import org.apache.ibatis.transaction.Transaction;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Properties;

/**
 * Creates {@link Transaction} instances.
 *
 * @author Clinton Begin
 */
public interface ZTransactionFactory {

  /**
   * Sets transaction factory custom properties.
   * @param props
   */
  default void setProperties(Properties props) {
    // NOP
  }

  /**
   * Creates a {@link Transaction} out of an existing connection.
   * @param conn Existing database connection
   * @return Transaction
   * @since 3.1.0
   */
  ZTransaction newTransaction(Connection conn);

  /**
   * Creates a {@link Transaction} out of a datasource.
   * @param dataSource DataSource to take the connection from
   * @param level Desired isolation level
   * @param autoCommit Desired autocommit
   * @return Transaction
   * @since 3.1.0
   */
  ZTransaction newTransaction(DataSource dataSource, TransactionIsolationLevel level, boolean autoCommit);

}
