package com.zmy.base.binding;

import com.zmy.core.session.ZSqlSession;
import org.apache.ibatis.reflection.ExceptionUtil;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

public class ZMapperProxy<T> implements InvocationHandler {

    // MapperProxyFactory 可以创建 mapperInterface 接口的代理对象 创建的代理对象要实现的接口
    private final ZSqlSession sqlSession; // 记录关联的 SqlSession对象
    private final Class<T> mapperInterface; // Mapper接口对应的Class对象
    // 用于缓存MapperMethod对象，key是Mapper接口方法对应的Method对象，value是对应的MapperMethod对象。‘
    // MapperMethod对象会完成参数转换以及SQL语句的执行
    // 注意：MapperMethod中并不会记录任何状态信息，可以在多线程间共享
    private final Map<Method, MapperMethodInvoker> methodCache;

    // TODO  不知道什么用
    private static final Method privateLookupInMethod;
    private static final Constructor<MethodHandles.Lookup> lookupConstructor;
    private static final int ALLOWED_MODES = MethodHandles.Lookup.PRIVATE | MethodHandles.Lookup.PROTECTED
            | MethodHandles.Lookup.PACKAGE | MethodHandles.Lookup.PUBLIC;

    public ZMapperProxy(ZSqlSession sqlSession, Class<T> mapperInterface, Map<Method, MapperMethodInvoker> methodCache) {
        this.sqlSession = sqlSession;
        this.mapperInterface = mapperInterface;
        this.methodCache = methodCache;
    }

    // TODO
    static {
        Method privateLookupIn;
        try {
            privateLookupIn = MethodHandles.class.getMethod("privateLookupIn", Class.class, MethodHandles.Lookup.class);
        } catch (NoSuchMethodException e) {
            privateLookupIn = null;
        }
        privateLookupInMethod = privateLookupIn;

        Constructor<MethodHandles.Lookup> lookup = null;
        if (privateLookupInMethod == null) {
            // JDK 1.8
            try {
                lookup = MethodHandles.Lookup.class.getDeclaredConstructor(Class.class, int.class);
                lookup.setAccessible(true);
            } catch (NoSuchMethodException e) {
                throw new IllegalStateException(
                        "There is neither 'privateLookupIn(Class, Lookup)' nor 'Lookup(Class, int)' method in java.lang.invoke.MethodHandles.",
                        e);
            } catch (Exception e) {
                lookup = null;
            }
        }
        lookupConstructor = lookup;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try {
            // toString hashCode equals getClass等方法，无需走到执行SQL的流程
            if (Object.class.equals(method.getDeclaringClass())) {
                return method.invoke(this, args);
            } else {
                // 提升获取 mapperMethod 的效率，到 MapperMethodInvoker（内部接口） 的 invoke
                // 普通方法会走到 PlainMethodInvoker（内部类） 的 invoke
                return cachedInvoker(method).invoke(proxy, method, args, sqlSession);
            }
        } catch (Throwable t) {
            throw ExceptionUtil.unwrapThrowable(t);
        }
    }

    private MapperMethodInvoker cachedInvoker(Method method) throws Throwable {
        try {
            // Java8 中 Map 的方法，根据 key 获取值，如果值是 null，则把后面Object 的值赋给 key
            // 如果获取不到，就创建
            // 获取的是 MapperMethodInvoker（接口） 对象，只有一个invoke方法
            // 根据method 去methodCache中获取 如果返回空 则用第二个参数填充
            return methodCache.computeIfAbsent(method, m -> {
                if (m.isDefault()) {
                    // 接口的默认方法(Java8)，只要实现接口都会继承接口的默认方法，例如 List.sort()
                    try {
                        if (privateLookupInMethod == null) {
                            return new DefaultMethodInvoker(getMethodHandleJava8(method));
                        } else {
                            return new DefaultMethodInvoker(getMethodHandleJava9(method));
                        }
                    } catch (IllegalAccessException | InstantiationException | InvocationTargetException
                             | NoSuchMethodException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    // 创建了一个 MapperMethod
                    return new PlainMethodInvoker(new ZMapperMethod(mapperInterface, method, sqlSession.getConfiguration()));
                }
            });
        } catch (RuntimeException re) {
            Throwable cause = re.getCause();
            throw cause == null ? re : cause;
        }
    }

    private MethodHandle getMethodHandleJava9(Method method)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        final Class<?> declaringClass = method.getDeclaringClass();
        return ((MethodHandles.Lookup) privateLookupInMethod.invoke(null, declaringClass, MethodHandles.lookup())).findSpecial(
                declaringClass, method.getName(), MethodType.methodType(method.getReturnType(), method.getParameterTypes()),
                declaringClass);
    }
    private MethodHandle getMethodHandleJava8(Method method)
            throws IllegalAccessException, InstantiationException, InvocationTargetException {
        final Class<?> declaringClass = method.getDeclaringClass();
        return lookupConstructor.newInstance(declaringClass, ALLOWED_MODES).unreflectSpecial(method, declaringClass);
    }

    private static class DefaultMethodInvoker implements MapperMethodInvoker {
        private final MethodHandle methodHandle;

        public DefaultMethodInvoker(MethodHandle methodHandle) {
            super();
            this.methodHandle = methodHandle;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args, ZSqlSession sqlSession) throws Throwable {
            return methodHandle.bindTo(proxy).invokeWithArguments(args);
        }
    }

    interface MapperMethodInvoker {
        Object invoke(Object proxy, Method method, Object[] args, ZSqlSession sqlSession) throws Throwable;
    }

    private static class PlainMethodInvoker implements MapperMethodInvoker {
        private final ZMapperMethod mapperMethod;

        public PlainMethodInvoker(ZMapperMethod mapperMethod) {
            super();
            this.mapperMethod = mapperMethod;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args, ZSqlSession sqlSession) throws Throwable {
            // SQL执行的真正起点
            return mapperMethod.execute(sqlSession, args);
        }
    }
}
