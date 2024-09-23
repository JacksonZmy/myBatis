package com.zmy.base.transaction.exception;

import org.apache.ibatis.exceptions.PersistenceException;

/**
 * @author Clinton Begin
 */
public class ZTransactionException extends PersistenceException {

  private static final long serialVersionUID = -433589569461084605L;

  public ZTransactionException() {
    super();
  }

  public ZTransactionException(String message) {
    super(message);
  }

  public ZTransactionException(String message, Throwable cause) {
    super(message, cause);
  }

  public ZTransactionException(Throwable cause) {
    super(cause);
  }

}
