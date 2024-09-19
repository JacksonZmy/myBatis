package com.zmy.core.executor.statement;

import com.zmy.base.type.ZTypeHandlerRegistry;
import com.zmy.core.executor.parameter.ZParameterHandler;
import com.zmy.core.executor.resultset.ZResultSetHandler;
import com.zmy.core.session.ZConfiguration;
import com.zmy.core.executor.ZExecutor;
import com.zmy.core.mapping.ZBoundSql;
import com.zmy.core.mapping.ZMappedStatement;
import com.zmy.core.session.ZResultHandler;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.executor.ExecutorException;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.executor.statement.StatementUtil;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.reflection.factory.DefaultObjectFactory;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public abstract class ZBaseStatementHandler implements ZStatementHandler{

    protected ZBoundSql boundSql;
    protected final ZConfiguration configuration;
    protected final ObjectFactory objectFactory;
    protected final ZTypeHandlerRegistry typeHandlerRegistry;
    protected final ZParameterHandler parameterHandler;

    protected final ZResultSetHandler resultSetHandler;
    protected final ZExecutor executor;
    protected final ZMappedStatement mappedStatement;
    protected final RowBounds rowBounds;

//    protected final ResultSetHandler resultSetHandler;

    protected ZBaseStatementHandler(ZExecutor executor, ZMappedStatement mappedStatement,
                                    Object parameter, RowBounds rowBounds, ZResultHandler resultHandler, ZBoundSql boundSql) {
        this.configuration = mappedStatement.getConfiguration();
        this.executor = executor;
        this.mappedStatement = mappedStatement;
        this.rowBounds = rowBounds;

        this.typeHandlerRegistry = configuration.getTypeHandlerRegistry();
//        this.objectFactory = configuration.getObjectFactory();
        this.objectFactory = new DefaultObjectFactory();

        if (boundSql == null) { // issue #435, get the key before calculating the statement
//            generateKeys(parameterObject);
            boundSql = mappedStatement.getBoundSql(parameter);
        }
        this.boundSql = boundSql;

        // 创建了四大对象的其它两大对象 >>
        // 创建这两大对象的时候分别做了什么？
        this.parameterHandler = configuration.newParameterHandler(mappedStatement, parameter, boundSql);
        this.resultSetHandler = configuration.newResultSetHandler(executor, mappedStatement, rowBounds, parameterHandler, resultHandler, boundSql);

    }

    // *********************** ZStatementHandler ***************************
    // TODO 后面需要根据不同子类返回不同的 statement 对象，交给子类去创建
    @Override
    public Statement prepare(Connection connection) throws SQLException {
        Statement statement = null;
//        try {
//            statement = instantiateStatement(connection);
//            setStatementTimeout(statement, transactionTimeout);
//            setFetchSize(statement);
//            return statement;
//        } catch (SQLException e) {
//            closeStatement(statement);
//            throw e;
//        } catch (Exception e) {
//            closeStatement(statement);
//            throw new ExecutorException("Error preparing statement.  Cause: " + e, e);
//        }
        statement = instantiateStatement(connection);
        return statement;
    }

    protected abstract Statement instantiateStatement(Connection connection) throws SQLException;

//    protected void setStatementTimeout(Statement stmt) throws SQLException {
//        Integer queryTimeout = null;
//        if (mappedStatement.getTimeout() != null) {
//            queryTimeout = mappedStatement.getTimeout();
//        } else if (configuration.getDefaultStatementTimeout() != null) {
//            queryTimeout = configuration.getDefaultStatementTimeout();
//        }
//        if (queryTimeout != null) {
//            stmt.setQueryTimeout(queryTimeout);
//        }
//        StatementUtil.applyTransactionTimeout(stmt, queryTimeout, transactionTimeout);
//    }

    @Override
    public ZBoundSql getBoundSql() {
        return boundSql;
    }

    @Override
    public ZParameterHandler getParameterHandler() {
        return parameterHandler;
    }
}
