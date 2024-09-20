package com.zmy.core.session;

import com.zmy.base.builder.xml.ZXMLConfigBuilder;
import com.zmy.core.session.defaults.ZDefaultSqlSessionFactory;
import org.apache.ibatis.exceptions.ExceptionFactory;
import org.apache.ibatis.executor.ErrorContext;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ZSqlSessionFactoryBuilder {

    public ZSqlSessionFactory build(InputStream inputStream) {
        return build(inputStream, null);
    }

    public ZSqlSessionFactory build(InputStream inputStream, String environment) {
        return build(inputStream, environment, null);
    }

    public ZSqlSessionFactory build(InputStream inputStream, String environment, Properties properties) {
        try {
            // 用于解析 mybatis-config.xml，同时创建了 Configuration 对象
            ZXMLConfigBuilder parser = new ZXMLConfigBuilder(inputStream, environment, properties);

            ZConfiguration configuration = parser.parse();
            // 解析XML，最终返回一个 DefaultSqlSessionFactory
            return build(configuration);
        } catch (Exception e) {
            throw ExceptionFactory.wrapException("构建 SqlSession 时出错。", e);
        } finally {
            ErrorContext.instance().reset();
            try {
                inputStream.close();
            } catch (IOException e) {
                // Intentionally ignore. Prefer previous error.
            }
        }
    }

    public ZSqlSessionFactory build(ZConfiguration config) {
        return new ZDefaultSqlSessionFactory(config);
    }

}
