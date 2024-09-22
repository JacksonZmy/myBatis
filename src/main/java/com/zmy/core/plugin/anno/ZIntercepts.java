package com.zmy.core.plugin.anno;

import java.lang.annotation.*;

/**
 * The annotation that specify target methods to intercept.
 *
 * <b>How to use:</b>
 * <pre>
 * &#064;Intercepts({&#064;Signature(
 *   type= Executor.class,
 *   method = "update",
 *   args = {MappedStatement.class ,Object.class})})
 * public class ExamplePlugin implements Interceptor {
 *   &#064;Override
 *   public Object intercept(Invocation invocation) throws Throwable {
 *     // implement pre-processing if needed
 *     Object returnObject = invocation.proceed();
 *     // implement post-processing if needed
 *     return returnObject;
 *   }
 * }
 * </pre>
 * @author Clinton Begin
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ZIntercepts {
  /**
   * Returns method signatures to intercept.
   *
   * @return method signatures
   */
  ZSignature[] value();
}

