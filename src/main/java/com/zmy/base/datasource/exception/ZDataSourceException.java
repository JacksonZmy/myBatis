package com.zmy.base.datasource.exception;

import org.apache.ibatis.exceptions.PersistenceException;

public class ZDataSourceException extends PersistenceException {
    private static final long serialVersionUID = -5251396250407091334L;

    public ZDataSourceException() {
        super();
    }

    public ZDataSourceException(String message) {
        super(message);
    }

    public ZDataSourceException(String message, Throwable cause) {
        super(message, cause);
    }

    public ZDataSourceException(Throwable cause) {
        super(cause);
    }

}
