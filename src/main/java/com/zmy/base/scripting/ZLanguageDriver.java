package com.zmy.base.scripting;

import com.zmy.core.executor.parameter.ZParameterHandler;
import com.zmy.core.session.ZConfiguration;
import com.zmy.core.mapping.ZBoundSql;
import com.zmy.core.mapping.ZMappedStatement;
import com.zmy.core.mapping.ZSqlSource;
import org.apache.ibatis.parsing.XNode;

public interface ZLanguageDriver {

    ZParameterHandler createParameterHandler(ZMappedStatement mappedStatement, Object parameterObject, ZBoundSql boundSql);

    ZSqlSource createSqlSource(ZConfiguration configuration, XNode script, Class<?> parameterType);

    ZSqlSource createSqlSource(ZConfiguration configuration, String script, Class<?> parameterType);

}
