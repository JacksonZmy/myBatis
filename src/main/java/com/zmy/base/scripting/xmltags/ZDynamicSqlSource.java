package com.zmy.base.scripting.xmltags;

import com.zmy.base.builder.ZSqlSourceBuilder;
import com.zmy.core.session.ZConfiguration;
import com.zmy.core.mapping.ZBoundSql;
import com.zmy.core.mapping.ZSqlSource;

public class ZDynamicSqlSource implements ZSqlSource {

    private final ZConfiguration configuration;
    private final ZSqlNode rootSqlNode;

    public ZDynamicSqlSource(ZConfiguration configuration, ZSqlNode rootSqlNode) {
        this.configuration = configuration;
        this.rootSqlNode = rootSqlNode;
    }

    public ZBoundSql getBoundSql(Object parameterObject) {
        ZDynamicContext context = new ZDynamicContext(this.configuration, parameterObject);
        this.rootSqlNode.apply(context);
        ZSqlSourceBuilder sqlSourceParser = new ZSqlSourceBuilder(this.configuration);
        Class<?> parameterType = parameterObject == null ? Object.class : parameterObject.getClass();
        ZSqlSource sqlSource = sqlSourceParser.parse(context.getSql(), parameterType, context.getBindings());
        ZBoundSql boundSql = sqlSource.getBoundSql(parameterObject);
//        context.getBindings().forEach(boundSql::setAdditionalParameter);
        return boundSql;
    }
}
