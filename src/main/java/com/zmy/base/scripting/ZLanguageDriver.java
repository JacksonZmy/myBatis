package com.zmy.base.scripting;

import com.zmy.core.executor.parameter.ZParameterHandler;
import com.zmy.core.session.ZConfiguration;
import com.zmy.core.mapping.ZBoundSql;
import com.zmy.core.mapping.ZMappedStatement;
import com.zmy.core.mapping.ZSqlSource;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.parsing.XNode;

public interface ZLanguageDriver {

    ZParameterHandler createParameterHandler(ZMappedStatement var1, Object var2, ZBoundSql var3);

    ZSqlSource createSqlSource(ZConfiguration var1, XNode var2, Class<?> var3);

    ZSqlSource createSqlSource(ZConfiguration var1, String var2, Class<?> var3);

}
