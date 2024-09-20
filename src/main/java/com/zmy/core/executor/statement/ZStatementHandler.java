package com.zmy.core.executor.statement;

import com.zmy.core.executor.parameter.ZParameterHandler;
import com.zmy.core.mapping.ZBoundSql;
import com.zmy.core.session.ZResultHandler;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public interface ZStatementHandler {

    /**
     * 从连接中获取一个Statement对象
     * @param connection 连接
     * @return 从连接中获取的对象
     * @throws SQLException
     */
    Statement prepare(Connection connection)
            throws SQLException;

    // 执行查询操作
    <E> List<E> query(Statement statement, ZResultHandler resultHandler)
            throws SQLException;

    // 执行DML操作
    int update(Statement statement) throws SQLException;

    // 获取BoundSql对象
    ZBoundSql getBoundSql();


    // 获取ParameterHandler对象
    ZParameterHandler getParameterHandler();

    // 绑定Statement执行时所需的实参
    void parameterize(Statement statement)
            throws SQLException;
}
