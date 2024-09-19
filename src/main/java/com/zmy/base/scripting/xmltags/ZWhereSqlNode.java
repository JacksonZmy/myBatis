package com.zmy.base.scripting.xmltags;

import com.zmy.core.session.ZConfiguration;
import java.util.Arrays;
import java.util.List;

public class ZWhereSqlNode extends ZTrimSqlNode{
    private static List<String> prefixList = Arrays.asList("AND ", "OR ", "AND\n", "OR\n", "AND\r", "OR\r", "AND\t", "OR\t");

    public ZWhereSqlNode(ZConfiguration configuration, ZSqlNode contents) {
        super(configuration, contents, "WHERE", prefixList, (String)null, (List)null);
    }
}
