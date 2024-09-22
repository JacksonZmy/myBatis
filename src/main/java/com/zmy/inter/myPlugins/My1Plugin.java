package com.zmy.inter.myPlugins;

import com.zmy.core.executor.ZExecutor;
import com.zmy.core.mapping.ZMappedStatement;
import com.zmy.core.plugin.ZInterceptor;
import com.zmy.core.plugin.ZInvocation;
import com.zmy.core.plugin.anno.ZIntercepts;
import com.zmy.core.plugin.anno.ZSignature;
import com.zmy.core.session.ZResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.util.Properties;

@ZIntercepts({
        @ZSignature(type = ZExecutor.class,
        method = "query",
        args = {ZMappedStatement.class, Object.class, RowBounds.class, ZResultHandler.class}),
        @ZSignature(type = ZExecutor.class,
                method = "close",
                args = {boolean.class})
        })
public class My1Plugin implements ZInterceptor {

    private int testProp;
    /**
     * 执行拦截逻辑的方法
     *
     * @param invocation
     * @return
     * @throws Throwable
     */
    @Override
    public Object intercept(ZInvocation invocation) throws Throwable {
        System.out.println("拦截之前============================");
        Object object = invocation.proceed();
        System.out.println("拦截之后============================");
        return object;
    }

    /**
     * 决定是否触发拦截方法
     *
     * @param target
     * @return
     */
    @Override
    public Object plugin(Object target) {
        return ZInterceptor.super.plugin(target);
    }

    @Override
    public void setProperties(Properties properties) {
        System.out.println("prop===============" + properties.getProperty("testProp"));
    }

    public int getTestProp() {
        return testProp;
    }

    public void setTestProp(int testProp) {
        this.testProp = testProp;
    }
}
