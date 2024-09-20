package com.zmy.core.executor.statement;

import com.zmy.core.executor.ZExecutor;
import com.zmy.core.mapping.ZBoundSql;
import com.zmy.core.mapping.ZMappedStatement;
import com.zmy.core.session.ZResultHandler;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.keygen.Jdbc3KeyGenerator;
import org.apache.ibatis.executor.keygen.KeyGenerator;
import org.apache.ibatis.executor.keygen.SelectKeyGenerator;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultSetType;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class ZSimpleStatementHandler extends ZBaseStatementHandler {

    public ZSimpleStatementHandler(ZExecutor executor, ZMappedStatement mappedStatement,
                                   Object parameter, RowBounds rowBounds, ZResultHandler resultHandler, ZBoundSql boundSql) {
        // 通过父类 BaseStatementHandler 的构造函数创建
        super(executor, mappedStatement, parameter, rowBounds, resultHandler, boundSql);
    }

    @Override
    protected Statement instantiateStatement(Connection connection) throws SQLException {
        if (mappedStatement.getResultSetType() == ResultSetType.DEFAULT) {
            return connection.createStatement();
        } else {
            return connection.createStatement(mappedStatement.getResultSetType().getValue(), ResultSet.CONCUR_READ_ONLY);
        }
    }

    @Override
    public <E> List<E> query(Statement statement, ZResultHandler resultHandler) throws SQLException {
        String sql = boundSql.getSql();
        statement.execute(sql);
        return resultSetHandler.handleResultSets(statement);
    }

    /**
     * TODO
     * @param statement
     * @return
     * @throws SQLException
     */
    @Override
    public int update(Statement statement) throws SQLException {
//        String sql = boundSql.getSql();
//        Object parameterObject = boundSql.getParameterObject();
//        KeyGenerator keyGenerator = mappedStatement.getKeyGenerator();
//        int rows;
//        if (keyGenerator instanceof Jdbc3KeyGenerator) {
//            statement.execute(sql, Statement.RETURN_GENERATED_KEYS);
//            rows = statement.getUpdateCount();
//            keyGenerator.processAfter(executor, mappedStatement, statement, parameterObject);
//        } else if (keyGenerator instanceof SelectKeyGenerator) {
//            statement.execute(sql);
//            rows = statement.getUpdateCount();
//            keyGenerator.processAfter(executor, mappedStatement, statement, parameterObject);
//        } else {
//            statement.execute(sql);
//            rows = statement.getUpdateCount();
//        }
//        return rows;
        return 0;
    }

    @Override
    public void parameterize(Statement statement) {
        // N/A
    }
}
