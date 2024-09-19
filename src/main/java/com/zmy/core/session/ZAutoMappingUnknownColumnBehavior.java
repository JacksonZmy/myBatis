package com.zmy.core.session;

import com.zmy.core.mapping.ZMappedStatement;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.session.AutoMappingUnknownColumnBehavior;
import org.apache.ibatis.session.SqlSessionException;

public enum ZAutoMappingUnknownColumnBehavior {

    /**
     * Do nothing (Default).
     */
    NONE {
        @Override
        public void doAction(ZMappedStatement mappedStatement, String columnName, String property, Class<?> propertyType) {
            // do nothing
        }
    },

    /**
     * Output warning log.
     * Note: The log level of {@code 'org.apache.ibatis.session.AutoMappingUnknownColumnBehavior'} must be set to {@code WARN}.
     */
    WARNING {
        @Override
        public void doAction(ZMappedStatement mappedStatement, String columnName, String property, Class<?> propertyType) {
            ZAutoMappingUnknownColumnBehavior.LogHolder.log.warn(buildMessage(mappedStatement, columnName, property, propertyType));
        }
    },

    /**
     * Fail mapping.
     * Note: throw {@link SqlSessionException}.
     */
    FAILING {
        @Override
        public void doAction(ZMappedStatement mappedStatement, String columnName, String property, Class<?> propertyType) {
            throw new SqlSessionException(buildMessage(mappedStatement, columnName, property, propertyType));
        }
    };

    /**
     * Perform the action when detects an unknown column (or unknown property type) of automatic mapping target.
     * @param mappedStatement current mapped statement
     * @param columnName column name for mapping target
     * @param propertyName property name for mapping target
     * @param propertyType property type for mapping target (If this argument is not null, {@link org.apache.ibatis.type.TypeHandler} for property type is not registered)
     */
    public abstract void doAction(ZMappedStatement mappedStatement, String columnName, String propertyName, Class<?> propertyType);

    /**
     * build error message.
     */
    private static String buildMessage(ZMappedStatement mappedStatement, String columnName, String property, Class<?> propertyType) {
        return new StringBuilder("Unknown column is detected on '")
                .append(mappedStatement.getId())
                .append("' auto-mapping. Mapping parameters are ")
                .append("[")
                .append("columnName=").append(columnName)
                .append(",").append("propertyName=").append(property)
                .append(",").append("propertyType=").append(propertyType != null ? propertyType.getName() : null)
                .append("]")
                .toString();
    }

    private static class LogHolder {
        private static final Log log = LogFactory.getLog(AutoMappingUnknownColumnBehavior.class);
    }

}
