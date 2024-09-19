package com.zmy.base.scripting.xmltags;

import com.zmy.core.session.ZConfiguration;
import org.apache.ibatis.ognl.OgnlContext;
import org.apache.ibatis.ognl.OgnlRuntime;
import org.apache.ibatis.ognl.PropertyAccessor;
import org.apache.ibatis.reflection.MetaObject;

import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

public class ZDynamicContext {
    public static final String PARAMETER_OBJECT_KEY = "_parameter";
    public static final String DATABASE_ID_KEY = "_databaseId";
    private final ZDynamicContext.ContextMap bindings;
    private final StringJoiner sqlBuilder = new StringJoiner(" ");
    private int uniqueNumber = 0;

    public ZDynamicContext(ZConfiguration configuration, Object parameterObject) {
        if (parameterObject != null && !(parameterObject instanceof Map)) {
//            MetaObject metaObject = configuration.newMetaObject(parameterObject);
            MetaObject metaObject = null;
            boolean existsTypeHandler = configuration.getTypeHandlerRegistry().hasTypeHandler(parameterObject.getClass());
            this.bindings = new ZDynamicContext.ContextMap(metaObject, existsTypeHandler);
        } else {
            this.bindings = new ZDynamicContext.ContextMap((MetaObject)null, false);
        }

        this.bindings.put("_parameter", parameterObject);
        this.bindings.put("_databaseId", configuration.getDatabaseId());
    }

    public Map<String, Object> getBindings() {
        return this.bindings;
    }

    public void bind(String name, Object value) {
        this.bindings.put(name, value);
    }

    public void appendSql(String sql) {
        this.sqlBuilder.add(sql);
    }

    public String getSql() {
        return this.sqlBuilder.toString().trim();
    }

    public int getUniqueNumber() {
        return this.uniqueNumber++;
    }

    static {
        OgnlRuntime.setPropertyAccessor(ZDynamicContext.ContextMap.class, new ZDynamicContext.ContextAccessor());
    }

    static class ContextAccessor implements PropertyAccessor {
        ContextAccessor() {
        }

        public Object getProperty(Map context, Object target, Object name) {
            Map map = (Map)target;
            Object result = map.get(name);
            if (!map.containsKey(name) && result == null) {
                Object parameterObject = map.get("_parameter");
                return parameterObject instanceof Map ? ((Map)parameterObject).get(name) : null;
            } else {
                return result;
            }
        }

        public void setProperty(Map context, Object target, Object name, Object value) {
            Map<Object, Object> map = (Map)target;
            map.put(name, value);
        }

        public String getSourceAccessor(OgnlContext arg0, Object arg1, Object arg2) {
            return null;
        }

        public String getSourceSetter(OgnlContext arg0, Object arg1, Object arg2) {
            return null;
        }
    }

    static class ContextMap extends HashMap<String, Object> {
        private static final long serialVersionUID = 2977601501966151582L;
        private final MetaObject parameterMetaObject;
        private final boolean fallbackParameterObject;

        public ContextMap(MetaObject parameterMetaObject, boolean fallbackParameterObject) {
            this.parameterMetaObject = parameterMetaObject;
            this.fallbackParameterObject = fallbackParameterObject;
        }

        public Object get(Object key) {
            String strKey = (String)key;
            if (super.containsKey(strKey)) {
                return super.get(strKey);
            } else if (this.parameterMetaObject == null) {
                return null;
            } else {
                return this.fallbackParameterObject && !this.parameterMetaObject.hasGetter(strKey) ? this.parameterMetaObject.getOriginalObject() : this.parameterMetaObject.getValue(strKey);
            }
        }
    }
}
