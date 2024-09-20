package com.zmy.base.binding;

import com.zmy.core.session.ZSqlSession;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 负责创建 MapperProxy 对象
 * @param <T>
 */
public class ZMapperProxyFactory<T> {

    // 缓存
    private final Map<Method, ZMapperProxy.MapperMethodInvoker> methodCache = new ConcurrentHashMap<>();

    // MapperProxyFactory 可以创建 mapperInterface 接口的代理对象 创建的代理对象要实现的接口
    private final Class<T> mapperInterface;

    public ZMapperProxyFactory(Class<T> mapperInterface) {
        this.mapperInterface = mapperInterface;
    }

    /**
     * 创建实现了 mapperInterface 接口的代理对象
     */
    protected T newInstance(ZMapperProxy<T> mapperProxy) {
        // 1：类加载器:2：被代理类实现的接口、3：实现了 InvocationHandler 的触发管理类
        return  (T)Proxy.newProxyInstance(mapperInterface.getClassLoader(), new Class[] { mapperInterface }, mapperProxy);
    }

    /**
     * 根据 sqlSession 类型创建实例
     * @param sqlSession
     * @return
     *
     * TODO 缓存
     */
    public T newInstance(ZSqlSession sqlSession) {
        final ZMapperProxy<T> mapperProxy = new ZMapperProxy<>(sqlSession, mapperInterface, methodCache);
//        final ZMapperProxy<T> mapperProxy = new ZMapperProxy<>(sqlSession, mapperInterface, null);
        return newInstance(mapperProxy);
    }
}
