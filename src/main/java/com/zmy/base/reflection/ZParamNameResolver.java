package com.zmy.base.reflection;

import com.zmy.core.session.ZConfiguration;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.binding.MapperMethod;
import org.apache.ibatis.reflection.ParamNameUtil;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class ZParamNameResolver {
    public static final String GENERIC_NAME_PREFIX = "param";

    private final SortedMap<Integer, String> names;

    private boolean hasParamAnnotation;

    public ZParamNameResolver(ZConfiguration config, Method method) {
        // 获取参数列表中的每个参数的类型
        final Class<?>[] paramTypes = method.getParameterTypes();
        // 获取参数列表中的 注解
        final Annotation[][] paramAnnotations = method.getParameterAnnotations();
        // 用于记录参数索引与参数名称的对应关系
        final SortedMap<Integer, String> map = new TreeMap<>();
        int paramCount = paramAnnotations.length;
        // get names from @Param annotations
        for (int paramIndex = 0; paramIndex < paramCount; paramIndex++) {
            if (isSpecialParameter(paramTypes[paramIndex])) {
                // 如果参数类型是 RowBounds或者 ResultHandler类型 则跳过对该参数的分析
                continue;
            }
            String name = null;
            for (Annotation annotation : paramAnnotations[paramIndex]) {
                // 判断是否使用了 @Param注解
                if (annotation instanceof Param) {
                    // 修改状态
                    hasParamAnnotation = true;
                    // 获取 @Param 注解的 value 值
                    name = ((Param) annotation).value();
                    break;
                }
            }
            if (name == null) {
                // @Param was not specified.
                if (config.isUseActualParamName()) {
                    // 使用实际名称作为其名称
                    name = getActualParamName(method, paramIndex);
                }
                if (name == null) {
                    // use the parameter index as the name ("0", "1", ...)
                    // gcode issue #71 使用参数的索引作为其名称
                    name = String.valueOf(map.size());
                }
            }
            // 记录到map集合中
            map.put(paramIndex, name);
        }
        // 初始化 names 集合
        names = Collections.unmodifiableSortedMap(map);
    }

    private static boolean isSpecialParameter(Class<?> clazz) {
        return RowBounds.class.isAssignableFrom(clazz) || ResultHandler.class.isAssignableFrom(clazz);
    }
    private String getActualParamName(Method method, int paramIndex) {
        return ParamNameUtil.getParamNames(method).get(paramIndex);
    }

    public Object getNamedParams(Object[] args) {
        final int paramCount = names.size();
        if (args == null || paramCount == 0) {
            return null;
        } else if (!hasParamAnnotation && paramCount == 1) {
            // 未使用 @Param 且参数个数只有一个
            return args[names.firstKey()];
        } else {
            // 处理使用了@Param注解或者 有多个参数的情况
            final Map<String, Object> param = new MapperMethod.ParamMap<>();
            int i = 0;
            for (Map.Entry<Integer, String> entry : names.entrySet()) {
                // 将参数名和实参名对应关系记录到param中
                param.put(entry.getValue(), args[entry.getKey()]);
                // add generic param names (param1, param2, ...)
                final String genericParamName = GENERIC_NAME_PREFIX + (i + 1);
                // ensure not to overwrite parameter named with @Param
                // 如果@Param注解 指定的参数名称就是 param+索引 的格式 则不需要添加
                if (!names.containsValue(genericParamName)) {
                    // 把key为 param1 ...  和 实际参数保存到 map集合中
                    param.put(genericParamName, args[entry.getKey()]);
                }
                i++;
            }
            return param;
        }
    }
}
