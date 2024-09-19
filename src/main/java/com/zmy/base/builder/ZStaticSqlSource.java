package com.zmy.base.builder;

import com.zmy.core.session.ZConfiguration;
import com.zmy.core.mapping.ZBoundSql;
import com.zmy.core.mapping.ZParameterMapping;
import com.zmy.core.mapping.ZSqlSource;
import java.util.List;

public class ZStaticSqlSource implements ZSqlSource {
    private final String sql;
    private final List<ZParameterMapping> parameterMappings;
    private final ZConfiguration configuration;

    public ZStaticSqlSource(ZConfiguration configuration, String sql) {
        this(configuration, sql, (List)null);
    }

    public ZStaticSqlSource(ZConfiguration configuration, String sql, List<ZParameterMapping> parameterMappings) {
        this.sql = sql;
        this.parameterMappings = parameterMappings;
        this.configuration = configuration;
    }

    public ZBoundSql getBoundSql(Object parameterObject) {
        return new ZBoundSql(this.configuration, this.sql, this.parameterMappings, parameterObject);
    }
}
