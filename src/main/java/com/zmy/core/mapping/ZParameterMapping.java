package com.zmy.core.mapping;

import com.zmy.base.type.ZTypeHandlerRegistry;
import com.zmy.core.session.ZConfiguration;
import lombok.Data;
import org.apache.ibatis.mapping.ParameterMode;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

import java.sql.ResultSet;

@Data
public class ZParameterMapping {

    private ZConfiguration configuration;

    private String property;
    private ParameterMode mode;
    private Class<?> javaType = Object.class;
    private JdbcType jdbcType;
    private Integer numericScale;
    private TypeHandler<?> typeHandler;
    private String resultMapId;
    private String jdbcTypeName;
    private String expression;

    private ZParameterMapping() {
    }

    public static class Builder {
        private ZParameterMapping parameterMapping = new ZParameterMapping();

        public Builder(ZConfiguration configuration, String property, TypeHandler<?> typeHandler) {
            parameterMapping.configuration = configuration;
            parameterMapping.property = property;
            parameterMapping.typeHandler = typeHandler;
            parameterMapping.mode = ParameterMode.IN;
        }

        public Builder(ZConfiguration configuration, String property, Class<?> javaType) {
            parameterMapping.configuration = configuration;
            parameterMapping.property = property;
            parameterMapping.javaType = javaType;
            parameterMapping.mode = ParameterMode.IN;
        }

        public ZParameterMapping.Builder mode(ParameterMode mode) {
            parameterMapping.mode = mode;
            return this;
        }

        public ZParameterMapping.Builder javaType(Class<?> javaType) {
            parameterMapping.javaType = javaType;
            return this;
        }

        public ZParameterMapping.Builder jdbcType(JdbcType jdbcType) {
            parameterMapping.jdbcType = jdbcType;
            return this;
        }

        public ZParameterMapping.Builder numericScale(Integer numericScale) {
            parameterMapping.numericScale = numericScale;
            return this;
        }

        public ZParameterMapping.Builder resultMapId(String resultMapId) {
            parameterMapping.resultMapId = resultMapId;
            return this;
        }

        public ZParameterMapping.Builder typeHandler(TypeHandler<?> typeHandler) {
            parameterMapping.typeHandler = typeHandler;
            return this;
        }

        public ZParameterMapping.Builder jdbcTypeName(String jdbcTypeName) {
            parameterMapping.jdbcTypeName = jdbcTypeName;
            return this;
        }

        public ZParameterMapping.Builder expression(String expression) {
            parameterMapping.expression = expression;
            return this;
        }

        public ZParameterMapping build() {
            resolveTypeHandler();
            validate();
            return parameterMapping;
        }

        private void validate() {
            if (ResultSet.class.equals(parameterMapping.javaType)) {
                if (parameterMapping.resultMapId == null) {
                    throw new IllegalStateException("Missing resultmap in property '"
                            + parameterMapping.property + "'.  "
                            + "Parameters of type java.sql.ResultSet require a resultmap.");
                }
            } else {
                if (parameterMapping.typeHandler == null) {
                    throw new IllegalStateException("Type handler was null on parameter mapping for property '"
                            + parameterMapping.property + "'. It was either not specified and/or could not be found for the javaType ("
                            + parameterMapping.javaType.getName() + ") : jdbcType (" + parameterMapping.jdbcType + ") combination.");
                }
            }
        }

        private void resolveTypeHandler() {
            if (parameterMapping.typeHandler == null && parameterMapping.javaType != null) {
                ZConfiguration configuration = parameterMapping.configuration;
                ZTypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
                parameterMapping.typeHandler = typeHandlerRegistry.getTypeHandler(parameterMapping.javaType, parameterMapping.jdbcType);
            }
        }

    }
}
