package com.zmy.base.builder;

import com.zmy.core.session.ZConfiguration;
import com.zmy.core.mapping.ZParameterMapping;
import com.zmy.core.mapping.ZSqlSource;
import org.apache.ibatis.builder.*;
import org.apache.ibatis.parsing.GenericTokenParser;
import org.apache.ibatis.parsing.TokenHandler;
import org.apache.ibatis.reflection.MetaClass;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.type.JdbcType;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ZSqlSourceBuilder extends ZBaseBuilder{
    private static final String PARAMETER_PROPERTIES = "javaType,jdbcType,mode,numericScale,resultMap,typeHandler,jdbcTypeName";

    public ZSqlSourceBuilder(ZConfiguration configuration) {
        super(configuration);
    }

    public ZSqlSource parse(String originalSql, Class<?> parameterType, Map<String, Object> additionalParameters) {
        ZSqlSourceBuilder.ZParameterMappingTokenHandler handler = new ZSqlSourceBuilder.ZParameterMappingTokenHandler(this.configuration, parameterType, additionalParameters);
        GenericTokenParser parser = new GenericTokenParser("#{", "}", handler);
        String sql = parser.parse(originalSql);
        return new ZStaticSqlSource(this.configuration, sql, handler.getParameterMappings());
    }

    private static class ZParameterMappingTokenHandler extends ZBaseBuilder implements TokenHandler {
        private List<ZParameterMapping> parameterMappings = new ArrayList();
        private Class<?> parameterType;
        private MetaObject metaParameters;

        public ZParameterMappingTokenHandler(ZConfiguration configuration, Class<?> parameterType, Map<String, Object> additionalParameters) {
            super(configuration);
            this.parameterType = parameterType;
            this.metaParameters = configuration.newMetaObject(additionalParameters);
        }

        public List<ZParameterMapping> getParameterMappings() {
            return this.parameterMappings;
        }

        public String handleToken(String content) {
            this.parameterMappings.add(this.buildParameterMapping(content));
            return "?";
        }

        private ZParameterMapping buildParameterMapping(String content) {
            Map<String, String> propertiesMap = this.parseParameterMapping(content);
            String property = (String)propertiesMap.get("property");
            Class propertyType;
            if (this.metaParameters.hasGetter(property)) {
                propertyType = this.metaParameters.getGetterType(property);
            } else if (this.typeHandlerRegistry.hasTypeHandler(this.parameterType)) {
                propertyType = this.parameterType;
            } else if (JdbcType.CURSOR.name().equals(propertiesMap.get("jdbcType"))) {
                propertyType = ResultSet.class;
            } else if (property != null && !Map.class.isAssignableFrom(this.parameterType)) {
                MetaClass metaClass = MetaClass.forClass(this.parameterType, this.configuration.getReflectorFactory());
                if (metaClass.hasGetter(property)) {
                    propertyType = metaClass.getGetterType(property);
                } else {
                    propertyType = Object.class;
                }
            } else {
                propertyType = Object.class;
            }

            ZParameterMapping.Builder builder = new ZParameterMapping.Builder(this.configuration, property, propertyType);
            Class<?> javaType = propertyType;
            String typeHandlerAlias = null;
            Iterator var8 = propertiesMap.entrySet().iterator();

            while(var8.hasNext()) {
                Map.Entry<String, String> entry = (Map.Entry)var8.next();
                String name = (String)entry.getKey();
                String value = (String)entry.getValue();
                if ("javaType".equals(name)) {
                    javaType = this.resolveClass(value);
                    builder.javaType(javaType);
                } else if ("jdbcType".equals(name)) {
                    builder.jdbcType(this.resolveJdbcType(value));
                } else if ("mode".equals(name)) {
                    builder.mode(this.resolveParameterMode(value));
                } else if ("numericScale".equals(name)) {
                    builder.numericScale(Integer.valueOf(value));
                } else if ("resultMap".equals(name)) {
                    builder.resultMapId(value);
                } else if ("typeHandler".equals(name)) {
                    typeHandlerAlias = value;
                } else if ("jdbcTypeName".equals(name)) {
                    builder.jdbcTypeName(value);
                } else if (!"property".equals(name)) {
                    if ("expression".equals(name)) {
                        throw new BuilderException("Expression based parameters are not supported yet");
                    }

                    throw new BuilderException("An invalid property '" + name + "' was found in mapping #{" + content + "}.  Valid properties are " + "javaType,jdbcType,mode,numericScale,resultMap,typeHandler,jdbcTypeName");
                }
            }

            if (typeHandlerAlias != null) {
                builder.typeHandler(this.resolveTypeHandler(javaType, typeHandlerAlias));
            }

            return builder.build();
        }

        private Map<String, String> parseParameterMapping(String content) {
            try {
                return new ParameterExpression(content);
            } catch (BuilderException var3) {
                throw var3;
            } catch (Exception var4) {
                throw new BuilderException("Parsing error was found in mapping #{" + content + "}.  Check syntax #{property|(expression), var1=value1, var2=value2, ...} ", var4);
            }
        }
    }
}
