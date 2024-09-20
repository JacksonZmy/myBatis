package com.zmy.core.session;

import org.apache.ibatis.session.RowBounds;
import java.io.Closeable;
import java.util.List;

public interface ZSqlSession extends Closeable {

    <T> T selectOne(String statement, Object parameter);

    <E> List<E> selectList(String statement, Object parameter);

    <E> List<E> selectList(String statement, Object parameter, RowBounds rowBounds);


    <T> T getMapper(Class<T> type);

    ZConfiguration getConfiguration();

    // *********************** Closeable接口 *************
    @Override
    void close();
}
