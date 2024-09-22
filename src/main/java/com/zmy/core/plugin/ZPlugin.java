package com.zmy.core.plugin;

import com.zmy.core.plugin.anno.ZIntercepts;
import com.zmy.core.plugin.anno.ZSignature;
import org.apache.ibatis.plugin.PluginException;
import org.apache.ibatis.reflection.ExceptionUtil;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Clinton Begin
 */
public class ZPlugin implements InvocationHandler {

  private final Object target; // 目标对象
  private final ZInterceptor interceptor; // 拦截器
  private final Map<Class<?>, Set<Method>> signatureMap; // 记录 @Signature 注解的信息

  private ZPlugin(Object target, ZInterceptor interceptor, Map<Class<?>, Set<Method>> signatureMap) {
    this.target = target;
    this.interceptor = interceptor;
    this.signatureMap = signatureMap;
  }

  /**
   * 创建目标对象的代理对象
   *    目标对象 Executor  ParameterHandler  ResultSetHandler StatementHandler
   * @param target 目标对象
   * @param interceptor 拦截器
   * @return
   */
  public static Object wrap(Object target, ZInterceptor interceptor) {
    // 获取用户自定义 Interceptor中@Signature注解的信息
    // getSignatureMap 负责处理@Signature 注解
    Map<Class<?>, Set<Method>> signatureMap = getSignatureMap(interceptor);
    // 获取目标类型
    Class<?> type = target.getClass();
    // 获取目标类型 实现的所有的接口
    Class<?>[] interfaces = getAllInterfaces(type, signatureMap);
    // 如果目标类型有实现的接口 就创建代理对象
    if (interfaces.length > 0) {
      return Proxy.newProxyInstance(
          type.getClassLoader(),
          interfaces,
          new ZPlugin(target, interceptor, signatureMap));
    }
    // 否则原封不动的返回目标对象
    return target;
  }

  /**
   * 代理对象方法被调用时执行的代码
   * @param proxy
   * @param method
   * @param args
   * @return
   * @throws Throwable
   */
  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    try {
      // 获取当前方法所在类或接口中，可被当前Interceptor拦截的方法
      Set<Method> methods = signatureMap.get(method.getDeclaringClass());
      if (methods != null && methods.contains(method)) {
        // 当前调用的方法需要被拦截 执行拦截操作
        return interceptor.intercept(new ZInvocation(target, method, args));
      }
      // 不需要拦截 则调用 目标对象中的方法
      return method.invoke(target, args);
    } catch (Exception e) {
      throw ExceptionUtil.unwrapThrowable(e);
    }
  }

  private static Map<Class<?>, Set<Method>> getSignatureMap(ZInterceptor interceptor) {
    ZIntercepts interceptsAnnotation = interceptor.getClass().getAnnotation(ZIntercepts.class);
    // issue #251
    if (interceptsAnnotation == null) {
      throw new org.apache.ibatis.plugin.PluginException("No @Intercepts annotation was found in interceptor " + interceptor.getClass().getName());
    }
    ZSignature[] sigs = interceptsAnnotation.value();
    Map<Class<?>, Set<Method>> signatureMap = new HashMap<>();
    for (ZSignature sig : sigs) {
      Set<Method> methods = signatureMap.computeIfAbsent(sig.type(), k -> new HashSet<>());
      try {
        Method method = sig.type().getMethod(sig.method(), sig.args());
        methods.add(method);
      } catch (NoSuchMethodException e) {
        throw new PluginException("Could not find method on " + sig.type() + " named " + sig.method() + ". Cause: " + e, e);
      }
    }
    return signatureMap;
  }

  private static Class<?>[] getAllInterfaces(Class<?> type, Map<Class<?>, Set<Method>> signatureMap) {
    Set<Class<?>> interfaces = new HashSet<>();
    while (type != null) {
      for (Class<?> c : type.getInterfaces()) {
        if (signatureMap.containsKey(c)) {
          interfaces.add(c);
        }
      }
      type = type.getSuperclass();
    }
    return interfaces.toArray(new Class<?>[interfaces.size()]);
  }

}
