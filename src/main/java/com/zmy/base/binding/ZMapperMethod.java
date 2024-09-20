package com.zmy.base.binding;

import com.zmy.base.reflection.ZParamNameResolver;
import com.zmy.core.mapping.ZMappedStatement;
import com.zmy.core.session.ZConfiguration;
import com.zmy.core.session.ZSqlSession;
import org.apache.ibatis.annotations.Flush;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.binding.BindingException;
import org.apache.ibatis.binding.MapperMethod;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.StatementType;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.ParamNameResolver;
import org.apache.ibatis.reflection.TypeParameterResolver;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ZMapperMethod {
    // statement id （例如：com.gupaoedu.mapper.BlogMapper.selectBlogById） 和 SQL 类型
    private final SqlCommand command;
    // 方法签名，主要是返回值的类型
    private final MethodSignature method;

    public ZMapperMethod(Class<?> mapperInterface, Method method, ZConfiguration config) {
        this.command = new ZMapperMethod.SqlCommand(config, mapperInterface, method);
        this.method = new ZMapperMethod.MethodSignature(config, mapperInterface, method);
    }

    public Object execute(ZSqlSession sqlSession, Object[] args) {
        Object result;
//        switch (command.getType()) { // 根据SQL语句的类型调用SqlSession对应的方法
//            case INSERT: {
//                // 通过 ParamNameResolver 处理args[] 数组 将用户传入的实参和指定参数名称关联起来
//                Object param = method.convertArgsToSqlCommandParam(args);
//                // sqlSession.insert(command.getName(), param) 调用SqlSession的insert方法
//                // rowCountResult 方法会根据 method 字段中记录的方法的返回值类型对结果进行转换
//                result = rowCountResult(sqlSession.insert(command.getName(), param));
//                break;
//            }
//            case UPDATE: {
//                Object param = method.convertArgsToSqlCommandParam(args);
//                result = rowCountResult(sqlSession.update(command.getName(), param));
//                break;
//            }
//            case DELETE: {
//                Object param = method.convertArgsToSqlCommandParam(args);
//                result = rowCountResult(sqlSession.delete(command.getName(), param));
//                break;
//            }
//            case SELECT:
//                if (method.returnsVoid() && method.hasResultHandler()) {
//                    // 返回值为空 且 ResultSet通过 ResultHandler处理的方法
//                    executeWithResultHandler(sqlSession, args);
//                    result = null;
//                } else if (method.returnsMany()) {
//                    result = executeForMany(sqlSession, args);
//                } else if (method.returnsMap()) {
//                    result = executeForMap(sqlSession, args);
//                } else if (method.returnsCursor()) {
//                    result = executeForCursor(sqlSession, args);
//                } else {
//                    // 返回值为 单一对象的方法
//                    Object param = method.convertArgsToSqlCommandParam(args);
//                    // 普通 select 语句的执行入口 >>
//                    result = sqlSession.selectOne(command.getName(), param);
//                    if (method.returnsOptional()
//                            && (result == null || !method.getReturnType().equals(result.getClass()))) {
//                        result = Optional.ofNullable(result);
//                    }
//                }
//                break;
//            case FLUSH:
//                result = sqlSession.flushStatements();
//                break;
//            default:
//                throw new BindingException("Unknown execution method for: " + command.getName());
//        }


        //TODO **************************** select 里面弄人的方法*******************************************
        // 返回值为 单一对象的方法
        Object param = method.convertArgsToSqlCommandParam(args);
        // 普通 select 语句的执行入口
        result = sqlSession.selectOne(command.getName(), param);
        if (method.returnsOptional()
                && (result == null || !method.getReturnType().equals(result.getClass()))) {
            result = Optional.ofNullable(result);
        }

        if (result == null && method.getReturnType().isPrimitive() && !method.returnsVoid()) {
            throw new BindingException("Mapper method '" + command.getName()
                    + " attempted to return null from a method with a primitive return type (" + method.getReturnType() + ").");
        }
        return result;
    }

    public static class SqlCommand {

        private final String name; // SQL语句的的名称
        private final SqlCommandType type; // SQL 语句的类型

        public SqlCommand(ZConfiguration configuration, Class<?> mapperInterface, Method method) {
            // 获取方法名称
            final String methodName = method.getName();
            final Class<?> declaringClass = method.getDeclaringClass();
            ZMappedStatement ms = resolveMappedStatement(mapperInterface, methodName, declaringClass,
                    configuration);
            if (ms == null) {
                if (method.getAnnotation(Flush.class) != null) {
                    name = null;
                    type = SqlCommandType.FLUSH;
                } else {
                    throw new BindingException("Invalid bound statement (not found): "
                            + mapperInterface.getName() + "." + methodName);
                }
            } else {
                name = ms.getId();
                type = ms.getSqlCommandType();
                if (type == SqlCommandType.UNKNOWN) {
                    throw new BindingException("Unknown execution method for: " + name);
                }
            }
        }

        public String getName() {
            return name;
        }

        public SqlCommandType getType() {
            return type;
        }

        private ZMappedStatement resolveMappedStatement(Class<?> mapperInterface, String methodName,
                                                       Class<?> declaringClass, ZConfiguration configuration) {
            // statementId = Mapper接口全路径 + 方法名称 比如:com.gupaoedu.mapper.UserMapper
            String statementId = mapperInterface.getName() + "." + methodName;
            if (configuration.hasStatement(statementId)) {// 检查是否有该名称的SQL语句
                return configuration.getMappedStatement(statementId);
            } else if (mapperInterface.equals(declaringClass)) {
                return null;
            }
            // 如果Mapper接口还有父类 就递归处理
            for (Class<?> superInterface : mapperInterface.getInterfaces()) {
                if (declaringClass.isAssignableFrom(superInterface)) {
                    ZMappedStatement ms = resolveMappedStatement(superInterface, methodName,
                            declaringClass, configuration);
                    if (ms != null) {
                        return ms;
                    }
                }
            }
            return null;
        }
    }

    public static class MethodSignature {

//        private final boolean returnsMany; // 判断返回是否为 Collection类型或者数组类型
        private final boolean returnsMap; // 返回值是否为 Map类型
        private final boolean returnsVoid; // 返回值类型是否为 void
        private final boolean returnsCursor; // 返回值类型是否为 Cursor 类型
        private final boolean returnsOptional; // 返回值类型是否为 Optional 类型
        private final Class<?> returnType; // 返回值类型
        private final String mapKey; // 如果返回值类型为 Map  则 mapKey 记录了作为 key的 列名
        private final Integer resultHandlerIndex; // 用来标记该方法参数列表中 ResultHandler 类型参数的位置
        private final Integer rowBoundsIndex; // 用来标记该方法参数列表中 rowBounds 类型参数的位置
        private final ZParamNameResolver paramNameResolver; // 该方法对应的 ParamNameResolver 对象

        /**
         * 方法签名
         * @param configuration
         * @param mapperInterface
         * @param method
         */
        public MethodSignature(ZConfiguration configuration, Class<?> mapperInterface, Method method) {
            // 获取接口方法的返回类型
            Type resolvedReturnType = TypeParameterResolver.resolveReturnType(method, mapperInterface);
            if (resolvedReturnType instanceof Class<?>) {
                this.returnType = (Class<?>) resolvedReturnType;
            } else if (resolvedReturnType instanceof ParameterizedType) {
                this.returnType = (Class<?>) ((ParameterizedType) resolvedReturnType).getRawType();
            } else {
                this.returnType = method.getReturnType();
            }
            this.returnsVoid = void.class.equals(this.returnType);
//            this.returnsMany = configuration.getObjectFactory().isCollection(this.returnType) || this.returnType.isArray();
            this.returnsCursor = Cursor.class.equals(this.returnType);
            this.returnsOptional = Optional.class.equals(this.returnType);
            this.mapKey = getMapKey(method);
            this.returnsMap = this.mapKey != null;
            // getUniqueParamIndex 查找指定类型的参数在 参数列表中的位置
            this.rowBoundsIndex = getUniqueParamIndex(method, RowBounds.class);
            this.resultHandlerIndex = getUniqueParamIndex(method, ResultHandler.class);
            this.paramNameResolver = new ZParamNameResolver(configuration, method);
        }

        /**
         * 负责将args[] 数组 (用户传入) 转换为SQL语句对应的参数列表，它是通过 ParamNameResolver 来实现的
         * @return
         */
        public Object convertArgsToSqlCommandParam(Object[] args) {
            return paramNameResolver.getNamedParams(args);
        }

        public boolean hasRowBounds() {
            return rowBoundsIndex != null;
        }

        public RowBounds extractRowBounds(Object[] args) {
            return hasRowBounds() ? (RowBounds) args[rowBoundsIndex] : null;
        }

        public boolean hasResultHandler() {
            return resultHandlerIndex != null;
        }

        public ResultHandler extractResultHandler(Object[] args) {
            return hasResultHandler() ? (ResultHandler) args[resultHandlerIndex] : null;
        }

        public String getMapKey() {
            return mapKey;
        }

        public Class<?> getReturnType() {
            return returnType;
        }

//        public boolean returnsMany() {
//            return returnsMany;
//        }

        public boolean returnsMap() {
            return returnsMap;
        }

        public boolean returnsVoid() {
            return returnsVoid;
        }

        public boolean returnsCursor() {
            return returnsCursor;
        }

        /**
         * return whether return type is {@code java.util.Optional}.
         * @return return {@code true}, if return type is {@code java.util.Optional}
         * @since 3.5.0
         */
        public boolean returnsOptional() {
            return returnsOptional;
        }

        /**
         * 查找指定类型的参数在参数列表中的位置
         * @param method
         * @param paramType
         * @return
         */
        private Integer getUniqueParamIndex(Method method, Class<?> paramType) {
            Integer index = null;
            // 获取对应方法的参数列表
            final Class<?>[] argTypes = method.getParameterTypes();
            // 遍历
            for (int i = 0; i < argTypes.length; i++) {
                // 判断是否是需要查找的类型
                if (paramType.isAssignableFrom(argTypes[i])) {
                    // 记录对应类型在参数列表中的位置
                    if (index == null) {
                        index = i;
                    } else {
                        // RowBounds 和 ResultHandler 类型的参数只能有一个，不能重复出现
                        throw new BindingException(method.getName() + " cannot have multiple " + paramType.getSimpleName() + " parameters");
                    }
                }
            }
            return index;
        }

        private String getMapKey(Method method) {
            String mapKey = null;
            if (Map.class.isAssignableFrom(method.getReturnType())) {
                // 有使用@MapKey 注解
                final MapKey mapKeyAnnotation = method.getAnnotation(MapKey.class);
                if (mapKeyAnnotation != null) {
                    mapKey = mapKeyAnnotation.value();
                }
            }
            return mapKey;
        }
    }
}
