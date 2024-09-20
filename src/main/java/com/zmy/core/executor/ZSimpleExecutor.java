package com.zmy.core.executor;

import com.zmy.core.mapping.ZBoundSql;
import com.zmy.core.session.ZConfiguration;
import com.zmy.core.executor.statement.ZStatementHandler;
import com.zmy.core.mapping.ZMappedStatement;
import com.zmy.core.session.ZResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.transaction.Transaction;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;


public class ZSimpleExecutor extends ZBaseExecutor {

    public ZSimpleExecutor(ZConfiguration configuration, Transaction transaction) {
        super(configuration, transaction);
    }

    @Override
    public <T> List<T> doQuery(ZMappedStatement ms, Object parameter, RowBounds rowBounds, ZResultHandler resultHandler, ZBoundSql boundSql) throws SQLException {
        Statement stmt = null;
        try {
            // 从 ZMappedStatement 里面拿到 configuration
            ZConfiguration zConfiguration = ms.getConfiguration();
            // 通过 configuration 获取一个 handler
            ZStatementHandler handler = zConfiguration.newStatementHandler(wrapper, ms, rowBounds, parameter, resultHandler, boundSql);
            // 获取一个 Statement对象
            stmt = prepareStatement(handler);
            return handler.query(stmt, resultHandler);
        }finally {
            // 用完就关闭
            closeStatement(stmt);
        }
    }

    @Override
    protected int doUpdate(ZMappedStatement ms, Object parameter) throws SQLException {
        Statement stmt = null;
        try {
            // 从 ZMappedStatement 里面拿到 configuration
            ZConfiguration zConfiguration = ms.getConfiguration();
            // 通过 configuration 获取一个 handler,插入更新操作不需要resulthanlder，wrapper（参数）
            ZStatementHandler handler = zConfiguration.newStatementHandler(this, ms, new RowBounds(), parameter, null, null);
            // 获取一个 Statement对象
            stmt = prepareStatement(handler);
            return handler.update(stmt);
        }finally {
            // 用完就关闭
            closeStatement(stmt);
        }
    }

    protected void closeStatement(Statement statement) {
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                // 忽略
            }
        }
    }

    /**
     * 从事务处理器获取 connection 连接，根据 connection 连接从handler获取 statement 连接，设置参数返回
     * @param handler StatementHandler
     * @return Statement
     * @throws SQLException
     */
    private Statement prepareStatement(ZStatementHandler handler) throws SQLException {
        Statement stmt;
        Connection connection = getConnection();
        // 获取 Statement 对象
        stmt = handler.prepare(connection);
        // 为 Statement 设置参数
        handler.parameterize(stmt);
        return stmt;
    }

}
