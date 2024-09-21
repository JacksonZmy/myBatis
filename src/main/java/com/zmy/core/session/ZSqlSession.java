package com.zmy.core.session;

import org.apache.ibatis.session.RowBounds;
import java.io.Closeable;
import java.util.List;

public interface ZSqlSession extends Closeable {

    <T> T selectOne(String statement, Object parameter);

    <E> List<E> selectList(String statement);

    <E> List<E> selectList(String statement, Object parameter);

    <E> List<E> selectList(String statement, Object parameter, RowBounds rowBounds);

    void select(String statement, ZResultHandler handler);

    void select(String statement, Object parameter, ZResultHandler handler);

    void select(String statement, Object parameter, RowBounds rowBounds, ZResultHandler handler);


    int insert(String statement);

    int insert(String statement, Object parameter);

    int update(String statement);

    int update(String statement, Object parameter);

    int delete(String statement);

    int delete(String statement, Object parameter);

    <T> T getMapper(Class<T> type);


    void commit();

    void commit(boolean force);

    void rollback();

    void rollback(boolean force);

    ZConfiguration getConfiguration();

    // *********************** Closeable接口 *************
    @Override
    void close();
}
