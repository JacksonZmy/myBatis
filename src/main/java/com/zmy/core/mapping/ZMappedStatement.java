package com.zmy.core.mapping;

import com.zmy.base.scripting.ZLanguageDriver;
import com.zmy.core.session.ZConfiguration;
import lombok.Data;
import org.apache.ibatis.mapping.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Data
public final class ZMappedStatement {

    private String id;
    private String resource;
    private ZSqlSource sqlSource;
    private ZConfiguration configuration;
    private ZParameterMap parameterMap;
    private boolean hasNestedResultMaps;
    // RoutingStatementHandler 里面获取 statement 时用到（STATEMENT, PREPARED, CALLABLE）
    private StatementType statementType;
    private ResultSetType resultSetType;
    private List<ZResultMap> resultMaps;
    private String databaseId;
    private SqlCommandType sqlCommandType;
    private ZLanguageDriver lang;
    private String[] resultSets;
    public String[] getResultSets() {
        return resultSets;
    }
    /**
     * @deprecated Use {@link #getResultSets()}
     */
    @Deprecated
    public String[] getResulSets() {
        return resultSets;
    }

    public ZLanguageDriver getLang() {
        return lang;
    }

    public ZMappedStatement() {
    }


    public ZBoundSql getBoundSql(Object parameterObject) {
        ZBoundSql boundSql = sqlSource.getBoundSql(parameterObject);
        List<ZParameterMapping> parameterMappings = boundSql.getParameterMappings();
        if (parameterMappings == null || parameterMappings.isEmpty()) {
            boundSql = new ZBoundSql(null, boundSql.getSql(), parameterMap.getParameterMappings(), parameterObject);
        }

//        // check for nested result maps in parameter mappings (issue #30)
//        for (ParameterMapping pm : boundSql.getParameterMappings()) {
//            String rmId = pm.getResultMapId();
//            if (rmId != null) {
//                ResultMap rm = configuration.getResultMap(rmId);
//                if (rm != null) {
//                    hasNestedResultMaps |= rm.hasNestedResultMaps();
//                }
//            }
//        }

        return boundSql;
    }
    public String getResource() {
        return resource;
    }

    public static class Builder {
        private ZMappedStatement mappedStatement = new ZMappedStatement();

        public Builder(ZConfiguration configuration, String id, ZSqlSource sqlSource, SqlCommandType sqlCommandType) {
            mappedStatement.configuration = configuration;
            mappedStatement.id = id;
            mappedStatement.sqlSource = sqlSource;
            mappedStatement.statementType = StatementType.PREPARED;
            mappedStatement.resultSetType = ResultSetType.DEFAULT;
            mappedStatement.parameterMap = new ZParameterMap.Builder(configuration, "defaultParameterMap", null, new ArrayList<>()).build();
            mappedStatement.resultMaps = new ArrayList<>();
            mappedStatement.sqlCommandType = sqlCommandType;
//            mappedStatement.keyGenerator = configuration.isUseGeneratedKeys() && SqlCommandType.INSERT.equals(sqlCommandType) ? Jdbc3KeyGenerator.INSTANCE : NoKeyGenerator.INSTANCE;
            mappedStatement.lang = configuration.getDefaultScriptingLanguageInstance();
        }

        public ZMappedStatement.Builder resource(String resource) {
            mappedStatement.resource = resource;
            return this;
        }

        public String id() {
            return mappedStatement.id;
        }

        public ZMappedStatement.Builder parameterMap(ZParameterMap parameterMap) {
            mappedStatement.parameterMap = parameterMap;
            return this;
        }

        public ZMappedStatement.Builder databaseId(String databaseId) {
            mappedStatement.databaseId = databaseId;
            return this;
        }

        public ZMappedStatement.Builder resultMaps(List<ZResultMap> resultMaps) {
            mappedStatement.resultMaps = resultMaps;
            for (ZResultMap resultMap : resultMaps) {
                mappedStatement.hasNestedResultMaps = mappedStatement.hasNestedResultMaps || resultMap.hasNestedResultMaps();
            }
            return this;
        }

        public ZMappedStatement.Builder statementType(StatementType statementType) {
            mappedStatement.statementType = statementType;
            return this;
        }

        public ZMappedStatement.Builder resultSetType(ResultSetType resultSetType) {
            mappedStatement.resultSetType = resultSetType == null ? ResultSetType.DEFAULT : resultSetType;
            return this;
        }
//
//        public MappedStatement.Builder resultSets(String resultSet) {
//            mappedStatement.resultSets = delimitedStringToArray(resultSet);
//            return this;
//        }

        public ZMappedStatement.Builder lang(ZLanguageDriver driver) {
            mappedStatement.lang = driver;
            return this;
        }

        /**
         * @deprecated Use {@link #resultSets}
         */
        @Deprecated
        public ZMappedStatement.Builder resulSets(String resultSet) {
            mappedStatement.resultSets = delimitedStringToArray(resultSet);
            return this;
        }


        public ZMappedStatement build() {
            assert mappedStatement.configuration != null;
            assert mappedStatement.id != null;
            assert mappedStatement.sqlSource != null;
            assert mappedStatement.lang != null;
            mappedStatement.resultMaps = Collections.unmodifiableList(mappedStatement.resultMaps);
            return mappedStatement;
        }
    }

    private static String[] delimitedStringToArray(String in) {
        if (in == null || in.trim().length() == 0) {
            return null;
        } else {
            return in.split(",");
        }
    }


}
