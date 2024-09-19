package com.zmy.core.mapping;

import com.zmy.core.session.ZConfiguration;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.property.PropertyTokenizer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ZBoundSql {
    private final String sql;
    private final List<ZParameterMapping> parameterMappings;
    private final Object parameterObject;
    private final Map<String, Object> additionalParameters;
    private final MetaObject metaParameters;

    public ZBoundSql(ZConfiguration configuration, String sql, List<ZParameterMapping> parameterMappings, Object parameterObject) {
        this.sql = sql;
        this.parameterMappings = parameterMappings;
        this.parameterObject = parameterObject;
        this.additionalParameters = new HashMap();
        this.metaParameters = configuration.newMetaObject(this.additionalParameters);
    }

    public String getSql() {
        return this.sql;
    }

    public List<ZParameterMapping> getParameterMappings() {
        return this.parameterMappings;
    }

    public Object getParameterObject() {
        return this.parameterObject;
    }

    public boolean hasAdditionalParameter(String name) {
        String paramName = (new PropertyTokenizer(name)).getName();
        return this.additionalParameters.containsKey(paramName);
    }

    public void setAdditionalParameter(String name, Object value) {
        this.metaParameters.setValue(name, value);
    }

    public Object getAdditionalParameter(String name) {
        return this.metaParameters.getValue(name);
    }

}
