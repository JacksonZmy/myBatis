package com.zmy.core.executor.statement;

import com.zmy.core.executor.ZExecutor;
import com.zmy.core.executor.parameter.ZParameterHandler;
import com.zmy.core.mapping.ZBoundSql;
import com.zmy.core.mapping.ZMappedStatement;
import com.zmy.core.session.ZResultHandler;
import org.apache.ibatis.executor.ExecutorException;
import org.apache.ibatis.session.RowBounds;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class ZRoutingStatementHandler implements ZStatementHandler{// 封装的有真正的 StatementHandler 对象

    // 封装的有真正的 StatementHandler 对象
    private final ZStatementHandler delegate;

    // TODO

    /**
     *
     * @param executor 包装处理器
     * @param ms
     * @param parameter
     * @param rowBounds
     * @param resultHandler
     * @param boundSql
     */
    public ZRoutingStatementHandler(ZExecutor executor, ZMappedStatement ms, Object parameter, RowBounds rowBounds, ZResultHandler resultHandler, ZBoundSql boundSql) {
        // StatementType 是怎么来的？ 增删改查标签中的 statementType="PREPARED"，默认值 PREPARED
        switch (ms.getStatementType()) {
            case STATEMENT:
                delegate = new ZSimpleStatementHandler(executor, ms, parameter, rowBounds, resultHandler, boundSql);
                break;
            case PREPARED:
                // 创建 StatementHandler 的时候做了什么？ >>
//                delegate = new PreparedStatementHandler(executor, ms, parameter, rowBounds, resultHandler, boundSql);
                delegate = new ZPreparedStatementHandler(executor, ms, parameter, rowBounds, resultHandler, boundSql);
                break;
            case CALLABLE:
//                delegate = new CallableStatementHandler(executor, ms, parameter, rowBounds, resultHandler, boundSql);
                delegate = new ZSimpleStatementHandler(executor, ms, parameter, rowBounds, resultHandler, boundSql);
                break;
            default:
                throw new ExecutorException("Unknown statement type: " + ms.getStatementType());
        }

    }

    @Override
    public Statement prepare(Connection connection) throws SQLException {
        return delegate.prepare(connection);
    }

    @Override
    public <E> List<E> query(Statement statement, ZResultHandler resultHandler) throws SQLException {
        return delegate.query(statement, resultHandler);
    }

    @Override
    public ZBoundSql getBoundSql() {
        return delegate.getBoundSql();
    }

    @Override
    public ZParameterHandler getParameterHandler() {
        return delegate.getParameterHandler();
    }

    @Override
    public void parameterize(Statement statement) throws SQLException {
        delegate.parameterize(statement);
    }
}
