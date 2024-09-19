/**
 *    Copyright 2009-2024 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
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
 * Static SqlSource. It is faster than {@link DynamicSqlSource} because mappings are
 * calculated during startup.
 *
 * @since 3.2.0
 * @author Eduardo Macarron
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
