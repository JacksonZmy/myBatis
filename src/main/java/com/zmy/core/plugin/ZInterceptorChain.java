package com.zmy.core.plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * InterceptorChain 记录所有的拦截器
 */
public class ZInterceptorChain {

  // 保存所有的 Interceptor  也就我所有的插件是保存在 Interceptors 这个List集合中的
  private final List<ZInterceptor> interceptors = new ArrayList<>();

  // 执行所有定义的所有插件
  public Object pluginAll(Object target) {
    for (ZInterceptor interceptor : interceptors) { // 获取拦截器链中的所有拦截器
      target = interceptor.plugin(target); // 创建对应的拦截器的代理对象
    }
    return target;
  }

  public void addInterceptor(ZInterceptor interceptor) {
    interceptors.add(interceptor);
  }

  public List<ZInterceptor> getInterceptors() {
    return Collections.unmodifiableList(interceptors);
  }

}
