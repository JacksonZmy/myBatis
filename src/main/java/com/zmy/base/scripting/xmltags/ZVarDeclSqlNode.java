package com.zmy.base.scripting.xmltags;

import org.apache.ibatis.scripting.xmltags.DynamicContext;
import org.apache.ibatis.scripting.xmltags.OgnlCache;

public class ZVarDeclSqlNode implements ZSqlNode{
    private final String name;
    private final String expression;

    public ZVarDeclSqlNode(String var, String exp) {
        this.name = var;
        this.expression = exp;
    }

    public boolean apply(ZDynamicContext context) {
        Object value = OgnlCache.getValue(this.expression, context.getBindings());
        context.bind(this.name, value);
        return true;
    }
}
