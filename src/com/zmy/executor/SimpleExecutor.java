package com.zmy.executor;

import com.zmy.beans.Test;
import com.zmy.executor.Excutor;
import com.zmy.statement.StatementHandler;

import java.sql.*;

public class SimpleExecutor implements Excutor {

    @Override
    public <T> T query(String statement, String parameter) {
        StatementHandler handler = new StatementHandler();
        return (T)handler.query(statement, parameter);
    }
}
