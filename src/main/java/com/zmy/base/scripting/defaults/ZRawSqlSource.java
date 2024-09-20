package com.zmy.base.scripting.defaults;

import com.zmy.base.builder.ZSqlSourceBuilder;
import com.zmy.base.scripting.xmltags.ZDynamicContext;
import com.zmy.base.scripting.xmltags.ZSqlNode;
import com.zmy.core.session.ZConfiguration;
import com.zmy.core.mapping.ZBoundSql;
import com.zmy.core.mapping.ZSqlSource;
import org.apache.ibatis.scripting.xmltags.DynamicSqlSource;

import java.util.HashMap;

/**
 * 静态 SqlSource。它比 DynamicSqlSource 更快，
 * 因为映射是在启动期间计算
 */
public class ZRawSqlSource implements ZSqlSource {

  private final ZSqlSource sqlSource;

  public ZRawSqlSource(ZConfiguration configuration, ZSqlNode rootSqlNode, Class<?> parameterType) {
    this(configuration, getSql(configuration, rootSqlNode), parameterType);
  }

  public ZRawSqlSource(ZConfiguration configuration, String sql, Class<?> parameterType) {
    ZSqlSourceBuilder sqlSourceParser = new ZSqlSourceBuilder(configuration);
    Class<?> clazz = parameterType == null ? Object.class : parameterType;
    sqlSource = sqlSourceParser.parse(sql, clazz, new HashMap<>());
  }

  private static String getSql(ZConfiguration configuration, ZSqlNode rootSqlNode) {
    ZDynamicContext context = new ZDynamicContext(configuration, null);
    rootSqlNode.apply(context);
    return context.getSql();
  }

  @Override
  public ZBoundSql getBoundSql(Object parameterObject) {
    return sqlSource.getBoundSql(parameterObject);
  }

}
