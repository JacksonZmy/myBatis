package com.zmy.base.builder;

import com.zmy.base.scripting.ZLanguageDriver;
import com.zmy.core.session.ZConfiguration;
import com.zmy.core.mapping.*;
import org.apache.ibatis.builder.BuilderException;
import org.apache.ibatis.builder.IncompleteElementException;
import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.executor.keygen.KeyGenerator;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.reflection.MetaClass;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

import java.util.*;

public class ZMapperBuilderAssistant extends ZBaseBuilder{

    private String currentNamespace;
    private final String resource;

    public ZMapperBuilderAssistant(ZConfiguration configuration, String resource) {
        super(configuration);
        ErrorContext.instance().resource(resource);
        this.resource = resource;
    }

    public String getCurrentNamespace() {
        return currentNamespace;
    }

    public void setCurrentNamespace(String currentNamespace) {
        if (currentNamespace == null) {
            throw new BuilderException("The mapper element requires a namespace attribute to be specified.");
        }

        if (this.currentNamespace != null && !this.currentNamespace.equals(currentNamespace)) {
            throw new BuilderException("没有找到 namespace："
                    + this.currentNamespace + "' but found '但是找到：" + currentNamespace + "。");
        }

        this.currentNamespace = currentNamespace;
    }

    public String applyCurrentNamespace(String base, boolean isReference) {
        if (base == null) {
            return null;
        }
        if (isReference) {
            // 它是否具有命名空间？
            if (base.contains(".")) {
                return base;
            }
        } else {
            // 它是否具有命名空间？
            if (base.startsWith(currentNamespace + ".")) {
                return base;
            }
            if (base.contains(".")) {
                throw new BuilderException("元素名称中不允许使用'.'，请将其从 " + base + " 中删除。");
            }
        }
        return currentNamespace + "." + base;
    }

    public ZParameterMap addParameterMap(String id, Class<?> parameterClass, List<ZParameterMapping> parameterMappings) {
        id = applyCurrentNamespace(id, false);
        ZParameterMap parameterMap = new ZParameterMap.Builder(configuration, id, parameterClass, parameterMappings).build();
        configuration.addParameterMap(parameterMap);
        return parameterMap;
    }

    public ZParameterMapping buildParameterMapping(
            Class<?> parameterType,
            String property,
            Class<?> javaType,
            JdbcType jdbcType,
            String resultMap,
            ParameterMode parameterMode,
            Class<? extends TypeHandler<?>> typeHandler,
            Integer numericScale) {
        resultMap = applyCurrentNamespace(resultMap, true);

        // Class parameterType = parameterMapBuilder.type();
        Class<?> javaTypeClass = resolveParameterJavaType(parameterType, property, javaType, jdbcType);
        TypeHandler<?> typeHandlerInstance = resolveTypeHandler(javaTypeClass, typeHandler);

        return new ZParameterMapping.Builder(configuration, property, javaTypeClass)
                .jdbcType(jdbcType)
                .resultMapId(resultMap)
                .mode(parameterMode)
                .numericScale(numericScale)
                .typeHandler(typeHandlerInstance)
                .build();
    }

    public ZResultMap addResultMap(
            String id,
            Class<?> type,
            String extend,
            ZDiscriminator discriminator,
            List<ZResultMapping> resultMappings,
            Boolean autoMapping) {
        id = applyCurrentNamespace(id, false);
        extend = applyCurrentNamespace(extend, true);

        if (extend != null) {
            if (!configuration.hasResultMap(extend)) {
                throw new IncompleteElementException("Could not find a parent resultmap with id '" + extend + "'");
            }
            ZResultMap resultMap = configuration.getResultMap(extend);
            List<ZResultMapping> extendedResultMappings = new ArrayList<>(resultMap.getResultMappings());
            extendedResultMappings.removeAll(resultMappings);
            // Remove parent constructor if this resultMap declares a constructor.
            boolean declaresConstructor = false;
            for (ZResultMapping resultMapping : resultMappings) {
                if (resultMapping.getFlags().contains(ResultFlag.CONSTRUCTOR)) {
                    declaresConstructor = true;
                    break;
                }
            }
            if (declaresConstructor) {
                extendedResultMappings.removeIf(resultMapping -> resultMapping.getFlags().contains(ResultFlag.CONSTRUCTOR));
            }
            resultMappings.addAll(extendedResultMappings);
        }
        ZResultMap resultMap = new ZResultMap.Builder(configuration, id, type, resultMappings, autoMapping)
                .discriminator(discriminator)
                .build();
        configuration.addResultMap(resultMap);
        return resultMap;
    }

    public ZDiscriminator buildDiscriminator(
            Class<?> resultType,
            String column,
            Class<?> javaType,
            JdbcType jdbcType,
            Class<? extends TypeHandler<?>> typeHandler,
            Map<String, String> discriminatorMap) {
        ZResultMapping resultMapping = buildResultMapping(
                resultType,
                null,
                column,
                javaType,
                jdbcType,
                null,
                null,
                null,
                null,
                typeHandler,
                new ArrayList<>(),
                null,
                null,
                false);
        Map<String, String> namespaceDiscriminatorMap = new HashMap<>();
        for (Map.Entry<String, String> e : discriminatorMap.entrySet()) {
            String resultMap = e.getValue();
            resultMap = applyCurrentNamespace(resultMap, true);
            namespaceDiscriminatorMap.put(e.getKey(), resultMap);
        }
        return new ZDiscriminator.Builder(configuration, resultMapping, namespaceDiscriminatorMap).build();
    }

    public ZMappedStatement addMappedStatement(
            String id,
            ZSqlSource sqlSource,
            StatementType statementType,
            SqlCommandType sqlCommandType,
            Integer fetchSize,
            Integer timeout,
            String parameterMap,
            Class<?> parameterType,
            String resultMap,
            Class<?> resultType,
            ResultSetType resultSetType,
            boolean flushCache,
            boolean useCache,
            boolean resultOrdered,
            KeyGenerator keyGenerator,
            String keyProperty,
            String keyColumn,
            String databaseId,
            ZLanguageDriver lang,
            String resultSets) {

        id = applyCurrentNamespace(id, false);
        boolean isSelect = sqlCommandType == SqlCommandType.SELECT;

        ZMappedStatement.Builder statementBuilder = new ZMappedStatement.Builder(configuration, id, sqlSource, sqlCommandType)
                .resource(resource)
//                .fetchSize(fetchSize)
//                .timeout(timeout)
                .statementType(statementType)
//                .keyGenerator(keyGenerator)
//                .keyProperty(keyProperty)
//                .keyColumn(keyColumn)
                .databaseId(databaseId)
                .lang(lang)
//                .resultOrdered(resultOrdered)
//                .resultSets(resultSets)
                .resultMaps(getStatementResultMaps(resultMap, resultType, id))
                .resultSetType(resultSetType);
//                .flushCacheRequired(valueOrDefault(flushCache, !isSelect))
//                .useCache(valueOrDefault(useCache, isSelect))
//                .cache(currentCache);

        ZParameterMap statementParameterMap = getStatementParameterMap(parameterMap, parameterType, id);
        if (statementParameterMap != null) {
            statementBuilder.parameterMap(statementParameterMap);
        }

        ZMappedStatement mappedStatement = statementBuilder.build();
        // 最关键的一步，在 Configuration 添加了 MappedStatement
        configuration.addMappedStatement(mappedStatement);
        return mappedStatement;
    }

    private <T> T valueOrDefault(T value, T defaultValue) {
        return value == null ? defaultValue : value;
    }

    private ZParameterMap getStatementParameterMap(
            String parameterMapName,
            Class<?> parameterTypeClass,
            String statementId) {
        parameterMapName = applyCurrentNamespace(parameterMapName, true);
        ZParameterMap parameterMap = null;
        if (parameterMapName != null) {
            try {
                parameterMap = configuration.getParameterMap(parameterMapName);
            } catch (IllegalArgumentException e) {
                throw new IncompleteElementException("Could not find parameter map " + parameterMapName, e);
            }
        } else if (parameterTypeClass != null) {
            List<ZParameterMapping> parameterMappings = new ArrayList<>();
            parameterMap = new ZParameterMap.Builder(
                    configuration,
                    statementId + "-Inline",
                    parameterTypeClass,
                    parameterMappings).build();
        }
        return parameterMap;
    }

    private List<ZResultMap> getStatementResultMaps(
            String resultMap,
            Class<?> resultType,
            String statementId) {
        resultMap = applyCurrentNamespace(resultMap, true);

        List<ZResultMap> resultMaps = new ArrayList<>();
        if (resultMap != null) {
            String[] resultMapNames = resultMap.split(",");
            for (String resultMapName : resultMapNames) {
                try {
                    resultMaps.add(configuration.getResultMap(resultMapName.trim()));
                } catch (IllegalArgumentException e) {
                    throw new IncompleteElementException("Could not find result map '" + resultMapName + "' referenced from '" + statementId + "'", e);
                }
            }
        } else if (resultType != null) {
            ZResultMap inlineResultMap = new ZResultMap.Builder(
                    configuration,
                    statementId + "-Inline",
                    resultType,
                    new ArrayList<>(),
                    null).build();
            resultMaps.add(inlineResultMap);
        }
        return resultMaps;
    }

    public ZResultMapping buildResultMapping(
            Class<?> resultType,
            String property,
            String column,
            Class<?> javaType,
            JdbcType jdbcType,
            String nestedSelect,
            String nestedResultMap,
            String notNullColumn,
            String columnPrefix,
            Class<? extends TypeHandler<?>> typeHandler,
            List<ResultFlag> flags,
            String resultSet,
            String foreignColumn,
            boolean lazy) {
        Class<?> javaTypeClass = resolveResultJavaType(resultType, property, javaType);
        TypeHandler<?> typeHandlerInstance = resolveTypeHandler(javaTypeClass, typeHandler);
        List<ZResultMapping> composites;
        if ((nestedSelect == null || nestedSelect.isEmpty()) && (foreignColumn == null || foreignColumn.isEmpty())) {
            composites = Collections.emptyList();
        } else {
            composites = parseCompositeColumnName(column);
        }
        return new ZResultMapping.Builder(configuration, property, column, javaTypeClass)
                .jdbcType(jdbcType)
                .nestedQueryId(applyCurrentNamespace(nestedSelect, true))
                .nestedResultMapId(applyCurrentNamespace(nestedResultMap, true))
                .resultSet(resultSet)
                .typeHandler(typeHandlerInstance)
                .flags(flags == null ? new ArrayList<>() : flags)
                .composites(composites)
                .notNullColumns(parseMultipleColumnNames(notNullColumn))
                .columnPrefix(columnPrefix)
                .foreignColumn(foreignColumn)
                .lazy(lazy)
                .build();
    }

    private Set<String> parseMultipleColumnNames(String columnName) {
        Set<String> columns = new HashSet<>();
        if (columnName != null) {
            if (columnName.indexOf(',') > -1) {
                StringTokenizer parser = new StringTokenizer(columnName, "{}, ", false);
                while (parser.hasMoreTokens()) {
                    String column = parser.nextToken();
                    columns.add(column);
                }
            } else {
                columns.add(columnName);
            }
        }
        return columns;
    }

    private List<ZResultMapping> parseCompositeColumnName(String columnName) {
        List<ZResultMapping> composites = new ArrayList<>();
        if (columnName != null && (columnName.indexOf('=') > -1 || columnName.indexOf(',') > -1)) {
            StringTokenizer parser = new StringTokenizer(columnName, "{}=, ", false);
            while (parser.hasMoreTokens()) {
                String property = parser.nextToken();
                String column = parser.nextToken();
                ZResultMapping complexResultMapping = new ZResultMapping.Builder(
                        configuration, property, column, configuration.getTypeHandlerRegistry().getUnknownTypeHandler()).build();
                composites.add(complexResultMapping);
            }
        }
        return composites;
    }

    private Class<?> resolveResultJavaType(Class<?> resultType, String property, Class<?> javaType) {
        if (javaType == null && property != null) {
            try {
                MetaClass metaResultType = MetaClass.forClass(resultType, configuration.getReflectorFactory());
                javaType = metaResultType.getSetterType(property);
            } catch (Exception e) {
                //ignore, following null check statement will deal with the situation
            }
        }
        if (javaType == null) {
            javaType = Object.class;
        }
        return javaType;
    }

    private Class<?> resolveParameterJavaType(Class<?> resultType, String property, Class<?> javaType, JdbcType jdbcType) {
        if (javaType == null) {
            if (JdbcType.CURSOR.equals(jdbcType)) {
                javaType = java.sql.ResultSet.class;
            } else if (Map.class.isAssignableFrom(resultType)) {
                javaType = Object.class;
            } else {
                MetaClass metaResultType = MetaClass.forClass(resultType, configuration.getReflectorFactory());
                javaType = metaResultType.getGetterType(property);
            }
        }
        if (javaType == null) {
            javaType = Object.class;
        }
        return javaType;
    }

    /** Backward compatibility signature. */
    public ZResultMapping buildResultMapping(Class<?> resultType, String property, String column, Class<?> javaType,
                                            JdbcType jdbcType, String nestedSelect, String nestedResultMap, String notNullColumn, String columnPrefix,
                                            Class<? extends TypeHandler<?>> typeHandler, List<ResultFlag> flags) {
        return buildResultMapping(
                resultType, property, column, javaType, jdbcType, nestedSelect,
                nestedResultMap, notNullColumn, columnPrefix, typeHandler, flags, null, null, false);
    }

    /**
     * @deprecated Use {@link Configuration#getLanguageDriver(Class)}
     */
//    @Deprecated
//    public LanguageDriver getLanguageDriver(Class<? extends LanguageDriver> langClass) {
//        return configuration.getLanguageDriver(langClass);
//    }

    /** Backward compatibility signature. */
    public ZMappedStatement addMappedStatement(String id, ZSqlSource sqlSource, StatementType statementType,
                                              SqlCommandType sqlCommandType, Integer fetchSize, Integer timeout, String parameterMap, Class<?> parameterType,
                                              String resultMap, Class<?> resultType, ResultSetType resultSetType, boolean flushCache, boolean useCache,
                                              boolean resultOrdered, KeyGenerator keyGenerator, String keyProperty, String keyColumn, String databaseId,
                                              ZLanguageDriver lang) {
        return addMappedStatement(
                id, sqlSource, statementType, sqlCommandType, fetchSize, timeout,
                parameterMap, parameterType, resultMap, resultType, resultSetType,
                flushCache, useCache, resultOrdered, keyGenerator, keyProperty,
                keyColumn, databaseId, lang, null);
    }

}
