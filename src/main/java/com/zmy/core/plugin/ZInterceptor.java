package com.zmy.core.plugin;

import java.util.Properties;

/**
 * 顶层接口,插件都需要实现这个接口并重写4个方法
 */
public interface ZInterceptor {

  // 执行拦截逻辑的方法
  Object intercept(ZInvocation invocation) throws Throwable;

  // 决定是否触发 intercept()方法
  default Object plugin(Object target) {
    return ZPlugin.wrap(target, this);
  }

  // 根据配置 初始化 Intercept 对象
  default void setProperties(Properties properties) {
    // NOP
  }

}
