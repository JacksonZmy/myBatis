package com.zmy.core.mapping;

import com.zmy.core.session.ZConfiguration;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.builder.BuilderException;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.mapping.ResultFlag;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.reflection.ParamNameUtil;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.*;

@Data
@NoArgsConstructor
public class ZResultMap {
    private ZConfiguration configuration;
    private String id;
    private Class<?> type;
    private List<ZResultMapping> resultMappings;
    private List<ZResultMapping> idResultMappings;
    private List<ZResultMapping> constructorResultMappings;
    private List<ZResultMapping> propertyResultMappings;
    private Set<String> mappedColumns;
    private Set<String> mappedProperties;
    private ZDiscriminator discriminator;
    private boolean hasNestedResultMaps;
    private boolean hasNestedQueries;
    private Boolean autoMapping;

    public boolean hasNestedResultMaps() {
        return hasNestedResultMaps;
    }

    public void forceNestedResultMaps() {
        hasNestedResultMaps = true;
    }

    public static class Builder {
        private static final Log log = LogFactory.getLog(ResultMap.Builder.class);

        private ZResultMap resultMap;

        public Builder(ZConfiguration configuration, String id, Class<?> type, List<ZResultMapping> resultMappings) {
            this(configuration, id, type, resultMappings, (Boolean)null);
        }

        public Builder(ZConfiguration configuration, String id, Class<?> type, List<ZResultMapping> resultMappings, Boolean autoMapping) {
            this.resultMap = new ZResultMap();
            this.resultMap.configuration = configuration;
            this.resultMap.id = id;
            this.resultMap.type = type;
            this.resultMap.resultMappings = resultMappings;
            this.resultMap.autoMapping = autoMapping;
        }

        public ZResultMap.Builder discriminator(ZDiscriminator discriminator) {
            this.resultMap.discriminator = discriminator;
            return this;
        }

        public Class<?> type() {
            return this.resultMap.type;
        }

        public ZResultMap build() {
            if (this.resultMap.id == null) {
                throw new IllegalArgumentException("ResultMaps must have an id");
            } else {
                this.resultMap.mappedColumns = new HashSet();
                this.resultMap.mappedProperties = new HashSet();
                this.resultMap.idResultMappings = new ArrayList();
                this.resultMap.constructorResultMappings = new ArrayList();
                this.resultMap.propertyResultMappings = new ArrayList();
                List<String> constructorArgNames = new ArrayList();
                Iterator var2 = this.resultMap.resultMappings.iterator();

                while(var2.hasNext()) {
                    ZResultMapping resultMapping = (ZResultMapping)var2.next();
                    this.resultMap.hasNestedQueries = this.resultMap.hasNestedQueries || resultMapping.getNestedQueryId() != null;
                    this.resultMap.hasNestedResultMaps = this.resultMap.hasNestedResultMaps || resultMapping.getNestedResultMapId() != null && resultMapping.getResultSet() == null;
                    String column = resultMapping.getColumn();
                    if (column != null) {
                        this.resultMap.mappedColumns.add(column.toUpperCase(Locale.ENGLISH));
                    } else if (resultMapping.isCompositeResult()) {
                        Iterator var5 = resultMapping.getComposites().iterator();

                        while(var5.hasNext()) {
                            ZResultMapping compositeResultMapping = (ZResultMapping)var5.next();
                            String compositeColumn = compositeResultMapping.getColumn();
                            if (compositeColumn != null) {
                                this.resultMap.mappedColumns.add(compositeColumn.toUpperCase(Locale.ENGLISH));
                            }
                        }
                    }

                    String property = resultMapping.getProperty();
                    if (property != null) {
                        this.resultMap.mappedProperties.add(property);
                    }

                    if (resultMapping.getFlags().contains(ResultFlag.CONSTRUCTOR)) {
                        this.resultMap.constructorResultMappings.add(resultMapping);
                        if (resultMapping.getProperty() != null) {
                            constructorArgNames.add(resultMapping.getProperty());
                        }
                    } else {
                        this.resultMap.propertyResultMappings.add(resultMapping);
                    }

                    if (resultMapping.getFlags().contains(ResultFlag.ID)) {
                        this.resultMap.idResultMappings.add(resultMapping);
                    }
                }

                if (this.resultMap.idResultMappings.isEmpty()) {
                    this.resultMap.idResultMappings.addAll(this.resultMap.resultMappings);
                }

                if (!constructorArgNames.isEmpty()) {
                    List<String> actualArgNames = this.argNamesOfMatchingConstructor(constructorArgNames);
                    if (actualArgNames == null) {
                        throw new BuilderException("Error in result map '" + this.resultMap.id + "'. Failed to find a constructor in '" + this.resultMap.getType().getName() + "' by arg names " + constructorArgNames + ". There might be more info in debug log.");
                    }

                    this.resultMap.constructorResultMappings.sort((o1, o2) -> {
                        int paramIdx1 = actualArgNames.indexOf(o1.getProperty());
                        int paramIdx2 = actualArgNames.indexOf(o2.getProperty());
                        return paramIdx1 - paramIdx2;
                    });
                }

                this.resultMap.resultMappings = Collections.unmodifiableList(this.resultMap.resultMappings);
                this.resultMap.idResultMappings = Collections.unmodifiableList(this.resultMap.idResultMappings);
                this.resultMap.constructorResultMappings = Collections.unmodifiableList(this.resultMap.constructorResultMappings);
                this.resultMap.propertyResultMappings = Collections.unmodifiableList(this.resultMap.propertyResultMappings);
                this.resultMap.mappedColumns = Collections.unmodifiableSet(this.resultMap.mappedColumns);
                return this.resultMap;
            }
        }

        private List<String> argNamesOfMatchingConstructor(List<String> constructorArgNames) {
            Constructor<?>[] constructors = this.resultMap.type.getDeclaredConstructors();
            Constructor[] var3 = constructors;
            int var4 = constructors.length;

            for(int var5 = 0; var5 < var4; ++var5) {
                Constructor<?> constructor = var3[var5];
                Class<?>[] paramTypes = constructor.getParameterTypes();
                if (constructorArgNames.size() == paramTypes.length) {
                    List<String> paramNames = this.getArgNames(constructor);
                    if (constructorArgNames.containsAll(paramNames) && this.argTypesMatch(constructorArgNames, paramTypes, paramNames)) {
                        return paramNames;
                    }
                }
            }

            return null;
        }

        private boolean argTypesMatch(List<String> constructorArgNames, Class<?>[] paramTypes, List<String> paramNames) {
            for(int i = 0; i < constructorArgNames.size(); ++i) {
                Class<?> actualType = paramTypes[paramNames.indexOf(constructorArgNames.get(i))];
                Class<?> specifiedType = ((ZResultMapping)this.resultMap.constructorResultMappings.get(i)).getJavaType();
                if (!actualType.equals(specifiedType)) {
                    if (log.isDebugEnabled()) {
                        log.debug("While building result map '" + resultMap.id
                                + "', found a constructor with arg names " + constructorArgNames
                                + ", but the type of '" + constructorArgNames.get(i)
                                + "' did not match. Specified: [" + specifiedType.getName() + "] Declared: ["
                                + actualType.getName() + "]");
                    }
                    return false;
                }
            }

            return true;
        }

        private List<String> getArgNames(Constructor<?> constructor) {
            List<String> paramNames = new ArrayList();
            List<String> actualParamNames = null;
            Annotation[][] paramAnnotations = constructor.getParameterAnnotations();
            int paramCount = paramAnnotations.length;

            for(int paramIndex = 0; paramIndex < paramCount; ++paramIndex) {
                String name = null;
                Annotation[] var8 = paramAnnotations[paramIndex];
                int var9 = var8.length;

                for(int var10 = 0; var10 < var9; ++var10) {
                    Annotation annotation = var8[var10];
                    if (annotation instanceof Param) {
                        name = ((Param)annotation).value();
                        break;
                    }
                }

                if (name == null && true) {
                    if (actualParamNames == null) {
                        actualParamNames = ParamNameUtil.getParamNames(constructor);
                    }

                    if (actualParamNames.size() > paramIndex) {
                        name = (String)actualParamNames.get(paramIndex);
                    }
                }

                paramNames.add(name != null ? name : "arg" + paramIndex);
            }

            return paramNames;
        }
    }
}
