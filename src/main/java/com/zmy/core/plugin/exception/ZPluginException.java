package com.zmy.core.plugin.exception;

import org.apache.ibatis.exceptions.PersistenceException;

/**
 * @author Clinton Begin
 */
public class ZPluginException extends PersistenceException {

  private static final long serialVersionUID = 8548771664564998595L;

  public ZPluginException() {
    super();
  }

  public ZPluginException(String message) {
    super(message);
  }

  public ZPluginException(String message, Throwable cause) {
    super(message, cause);
  }

  public ZPluginException(Throwable cause) {
    super(cause);
  }
}
