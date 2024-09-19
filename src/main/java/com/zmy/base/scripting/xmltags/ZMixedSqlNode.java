package com.zmy.base.scripting.xmltags;

import org.apache.ibatis.scripting.xmltags.DynamicContext;
import org.apache.ibatis.scripting.xmltags.SqlNode;

import java.util.List;

public class ZMixedSqlNode implements ZSqlNode{
    private final List<ZSqlNode> contents;

    public ZMixedSqlNode(List<ZSqlNode> contents) {
        this.contents = contents;
    }

    public boolean apply(ZDynamicContext context) {
        this.contents.forEach((node) -> {
            node.apply(context);
        });
        return true;
    }
}
