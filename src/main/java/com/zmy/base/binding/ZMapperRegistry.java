package com.zmy.base.binding;

import com.zmy.core.session.ZConfiguration;
import com.zmy.core.session.ZSqlSession;
import org.apache.ibatis.binding.BindingException;
import org.apache.ibatis.io.ResolverUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ZMapperRegistry {

    private final ZConfiguration config;

    // 记录 Mapper 接口和 MapperProxyFactory 之间的关系
    private final Map<Class<?>, ZMapperProxyFactory<?>> knownMappers = new HashMap<>();

    public ZMapperRegistry(ZConfiguration config) {
        this.config = config;
    }

    /**
     * 获取Mapper接口对应的代理对象
     */
    public <T> T getMapper(Class<T> type, ZSqlSession sqlSession) {
        // 获取Mapper接口对应的 MapperProxyFactory 对象
        final ZMapperProxyFactory<T> mapperProxyFactory = (ZMapperProxyFactory<T>) knownMappers.get(type);
        if (mapperProxyFactory == null) {
            throw new BindingException("在 MapperRegistry 中没有找到 " + type + " 类型");
        }
        try {
            return mapperProxyFactory.newInstance(sqlSession);
        } catch (Exception e) {
            throw new BindingException("获取 mapper 实例时出错，原因： " + e, e);
        }
    }

    public <T> boolean hasMapper(Class<T> type) {
        return knownMappers.containsKey(type);
    }

    public <T> void addMapper(Class<T> type) {
        this.knownMappers.put(type, new ZMapperProxyFactory<>(type));
    }

    public void addMappers(String packageName, Class<?> superType) {
        ResolverUtil<Class<?>> resolverUtil = new ResolverUtil<>();
        resolverUtil.find(new ResolverUtil.IsA(superType), packageName);
        Set<Class<? extends Class<?>>> mapperSet = resolverUtil.getClasses();
        // 循环添加
        for (Class<?> mapperClass : mapperSet) {
            addMapper(mapperClass);
        }
    }

    public void addMappers(String packageName) {
        addMappers(packageName, Object.class);
    }
}
