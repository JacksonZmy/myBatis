package com.zmy.core.mapping;

import com.zmy.base.type.ZTypeHandlerRegistry;
import com.zmy.core.session.ZConfiguration;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.mapping.ResultFlag;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@NoArgsConstructor
@Data
public class ZResultMapping {
    private ZConfiguration configuration;
    private String property;
    private String column;
    private Class<?> javaType;
    private JdbcType jdbcType;
    private TypeHandler<?> typeHandler;
    private String nestedResultMapId;
    private String nestedQueryId;
    private Set<String> notNullColumns;
    private String columnPrefix;
    private List<ResultFlag> flags;
    private List<ZResultMapping> composites;
    private String resultSet;
    private String foreignColumn;
    private boolean lazy;

    public boolean isCompositeResult() {
        return this.composites != null && !this.composites.isEmpty();
    }

    public static class Builder {
        private ZResultMapping resultMapping = new ZResultMapping();

        public Builder(ZConfiguration configuration, String property, String column, TypeHandler<?> typeHandler) {
            this(configuration, property);
            resultMapping.column = column;
            resultMapping.typeHandler = typeHandler;
        }

        public Builder(ZConfiguration configuration, String property, String column, Class<?> javaType) {
            this(configuration, property);
            resultMapping.column = column;
            resultMapping.javaType = javaType;
        }

        public Builder(ZConfiguration configuration, String property) {
            resultMapping.configuration = configuration;
            resultMapping.property = property;
            resultMapping.flags = new ArrayList<>();
            resultMapping.composites = new ArrayList<>();
            resultMapping.lazy = false;
        }

        public ZResultMapping.Builder javaType(Class<?> javaType) {
            resultMapping.javaType = javaType;
            return this;
        }

        public ZResultMapping.Builder jdbcType(JdbcType jdbcType) {
            resultMapping.jdbcType = jdbcType;
            return this;
        }

        public ZResultMapping.Builder nestedResultMapId(String nestedResultMapId) {
            resultMapping.nestedResultMapId = nestedResultMapId;
            return this;
        }

        public ZResultMapping.Builder nestedQueryId(String nestedQueryId) {
            resultMapping.nestedQueryId = nestedQueryId;
            return this;
        }

        public ZResultMapping.Builder resultSet(String resultSet) {
            resultMapping.resultSet = resultSet;
            return this;
        }

        public ZResultMapping.Builder foreignColumn(String foreignColumn) {
            resultMapping.foreignColumn = foreignColumn;
            return this;
        }

        public ZResultMapping.Builder notNullColumns(Set<String> notNullColumns) {
            resultMapping.notNullColumns = notNullColumns;
            return this;
        }

        public ZResultMapping.Builder columnPrefix(String columnPrefix) {
            resultMapping.columnPrefix = columnPrefix;
            return this;
        }

        public ZResultMapping.Builder flags(List<ResultFlag> flags) {
            resultMapping.flags = flags;
            return this;
        }

        public ZResultMapping.Builder typeHandler(TypeHandler<?> typeHandler) {
            resultMapping.typeHandler = typeHandler;
            return this;
        }

        public ZResultMapping.Builder composites(List<ZResultMapping> composites) {
            resultMapping.composites = composites;
            return this;
        }

        public ZResultMapping.Builder lazy(boolean lazy) {
            resultMapping.lazy = lazy;
            return this;
        }

        public ZResultMapping build() {
            // lock down collections
            resultMapping.flags = Collections.unmodifiableList(resultMapping.flags);
            resultMapping.composites = Collections.unmodifiableList(resultMapping.composites);
            resolveTypeHandler();
            validate();
            return resultMapping;
        }

        private void validate() {
            // Issue #697: cannot define both nestedQueryId and nestedResultMapId
            if (resultMapping.nestedQueryId != null && resultMapping.nestedResultMapId != null) {
                throw new IllegalStateException("Cannot define both nestedQueryId and nestedResultMapId in property " + resultMapping.property);
            }
            // Issue #5: there should be no mappings without typehandler
            if (resultMapping.nestedQueryId == null && resultMapping.nestedResultMapId == null && resultMapping.typeHandler == null) {
                throw new IllegalStateException("No typehandler found for property " + resultMapping.property);
            }
            // Issue #4 and GH #39: column is optional only in nested resultmaps but not in the rest
            if (resultMapping.nestedResultMapId == null && resultMapping.column == null && resultMapping.composites.isEmpty()) {
                throw new IllegalStateException("Mapping is missing column attribute for property " + resultMapping.property);
            }
            if (resultMapping.getResultSet() != null) {
                int numColumns = 0;
                if (resultMapping.column != null) {
                    numColumns = resultMapping.column.split(",").length;
                }
                int numForeignColumns = 0;
                if (resultMapping.foreignColumn != null) {
                    numForeignColumns = resultMapping.foreignColumn.split(",").length;
                }
                if (numColumns != numForeignColumns) {
                    throw new IllegalStateException("There should be the same number of columns and foreignColumns in property " + resultMapping.property);
                }
            }
        }

        private void resolveTypeHandler() {
            if (resultMapping.typeHandler == null && resultMapping.javaType != null) {
                ZConfiguration configuration = resultMapping.configuration;
                ZTypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
                resultMapping.typeHandler = typeHandlerRegistry.getTypeHandler(resultMapping.javaType, resultMapping.jdbcType);
            }
        }

        public ZResultMapping.Builder column(String column) {
            resultMapping.column = column;
            return this;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ZResultMapping that = (ZResultMapping) o;

        return property != null && property.equals(that.property);
    }

    @Override
    public int hashCode() {
        if (property != null) {
            return property.hashCode();
        } else if (column != null) {
            return column.hashCode();
        } else {
            return 0;
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ResultMapping{");
        //sb.append("configuration=").append(configuration); // configuration doesn't have a useful .toString()
        sb.append("property='").append(property).append('\'');
        sb.append(", column='").append(column).append('\'');
        sb.append(", javaType=").append(javaType);
        sb.append(", jdbcType=").append(jdbcType);
        //sb.append(", typeHandler=").append(typeHandler); // typeHandler also doesn't have a useful .toString()
        sb.append(", nestedResultMapId='").append(nestedResultMapId).append('\'');
        sb.append(", nestedQueryId='").append(nestedQueryId).append('\'');
        sb.append(", notNullColumns=").append(notNullColumns);
        sb.append(", columnPrefix='").append(columnPrefix).append('\'');
        sb.append(", flags=").append(flags);
        sb.append(", composites=").append(composites);
        sb.append(", resultSet='").append(resultSet).append('\'');
        sb.append(", foreignColumn='").append(foreignColumn).append('\'');
        sb.append(", lazy=").append(lazy);
        sb.append('}');
        return sb.toString();
    }
}
