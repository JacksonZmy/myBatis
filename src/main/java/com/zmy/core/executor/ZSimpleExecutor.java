package com.zmy.core.executor;

import com.zmy.core.statement.ZStatementHandler;

public class ZSimpleExecutor implements ZExcutor {

    @Override
    public <T> T query(String statement, String parameter) {
        ZStatementHandler handler = new ZStatementHandler();
        return (T)handler.query(statement, parameter);
    }
}
