package com.zmy.base.binding;

public class ZMapperProxyFactory<T> {

    // MapperProxyFactory 可以创建 mapperInterface 接口的代理对象 创建的代理对象要实现的接口
    private final Class<T> mapperInterface;

    public ZMapperProxyFactory(Class<T> mapperInterface) {
        this.mapperInterface = mapperInterface;
    }
}
