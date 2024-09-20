package com.zmy.base.scripting.xmltags;

import com.zmy.base.builder.ZBaseBuilder;
import com.zmy.base.scripting.defaults.ZRawSqlSource;
import com.zmy.core.session.ZConfiguration;
import com.zmy.core.mapping.ZSqlSource;
import org.apache.ibatis.builder.BuilderException;
import org.apache.ibatis.parsing.XNode;
import org.w3c.dom.NodeList;

import java.util.*;

/**
 * 解析并构建xml种的sql关键字，建造者模式
 * chose
 * otherwise
 * if
 * foreach
 * set
 * where
 * trim
 * bing
 */
public class ZXMLScriptBuilder extends ZBaseBuilder {

    private final XNode context; // select标签内容：<select id="selectOne" resultType="com.zmy.inter.beans.User">select</select>
    private boolean isDynamic; // 是不是动态sql
    private final Class<?> parameterType;
    private final Map<String, ZXMLScriptBuilder.NodeHandler> nodeHandlerMap; // 各个关键字handler

    public ZXMLScriptBuilder(ZConfiguration configuration, XNode context) {
        this(configuration, context, (Class)null);
    }

    public ZXMLScriptBuilder(ZConfiguration configuration, XNode context, Class<?> parameterType) {
        super(configuration);
        this.nodeHandlerMap = new HashMap();
        this.context = context;
        this.parameterType = parameterType;
        this.initNodeHandlerMap();
    }

    private void initNodeHandlerMap() {
        this.nodeHandlerMap.put("trim", new ZXMLScriptBuilder.ZTrimHandler());
        this.nodeHandlerMap.put("where", new ZXMLScriptBuilder.ZWhereHandler());
        this.nodeHandlerMap.put("set", new ZXMLScriptBuilder.ZSetHandler());
        this.nodeHandlerMap.put("foreach", new ZXMLScriptBuilder.ZForEachHandler());
        this.nodeHandlerMap.put("if", new ZXMLScriptBuilder.IfHandler());
        this.nodeHandlerMap.put("choose", new ZXMLScriptBuilder.ChooseHandler());
        this.nodeHandlerMap.put("when", new ZXMLScriptBuilder.IfHandler());
        this.nodeHandlerMap.put("otherwise", new ZXMLScriptBuilder.OtherwiseHandler());
        this.nodeHandlerMap.put("bind", new ZXMLScriptBuilder.BindHandler());
    }

    public ZSqlSource parseScriptNode() {
        ZMixedSqlNode rootSqlNode = this.parseDynamicTags(this.context);
        Object sqlSource;
        if (this.isDynamic) {
            sqlSource = new ZDynamicSqlSource(this.configuration, rootSqlNode);
        } else {
            sqlSource = new ZRawSqlSource(this.configuration, rootSqlNode, this.parameterType);
        }

        return (ZSqlSource)sqlSource;
    }

    protected ZMixedSqlNode parseDynamicTags(XNode node) {
        List<ZSqlNode> contents = new ArrayList();
        NodeList children = node.getNode().getChildNodes();

        for(int i = 0; i < children.getLength(); ++i) {
            XNode child = node.newXNode(children.item(i));
            String nodeName;
            if (child.getNode().getNodeType() != 4 && child.getNode().getNodeType() != 3) {
                if (child.getNode().getNodeType() == 1) {
                    nodeName = child.getNode().getNodeName();
                    ZXMLScriptBuilder.NodeHandler handler = (ZXMLScriptBuilder.NodeHandler)this.nodeHandlerMap.get(nodeName);
                    if (handler == null) {
                        throw new BuilderException("Unknown element <" + nodeName + "> in SQL statement.");
                    }

                    handler.handleNode(child, contents);
                    this.isDynamic = true;
                }
            } else {
                nodeName = child.getStringBody("");
                ZTextSqlNode textSqlNode = new ZTextSqlNode(nodeName);
                if (textSqlNode.isDynamic()) {
                    contents.add(textSqlNode);
                    this.isDynamic = true;
                } else {
                    contents.add(new ZStaticTextSqlNode(nodeName));
                }
            }
        }

        return new ZMixedSqlNode(contents);
    }

    private class ChooseHandler implements ZXMLScriptBuilder.NodeHandler {
        public ChooseHandler() {
        }

        public void handleNode(XNode nodeToHandle, List<ZSqlNode> targetContents) {
            List<ZSqlNode> whenSqlNodes = new ArrayList();
            List<ZSqlNode> otherwiseSqlNodes = new ArrayList();
            this.handleWhenOtherwiseNodes(nodeToHandle, whenSqlNodes, otherwiseSqlNodes);
            ZSqlNode defaultSqlNode = this.getDefaultSqlNode(otherwiseSqlNodes);
            ZChooseSqlNode chooseSqlNode = new ZChooseSqlNode(whenSqlNodes, defaultSqlNode);
            targetContents.add(chooseSqlNode);
        }

        private void handleWhenOtherwiseNodes(XNode chooseSqlNode, List<ZSqlNode> ifSqlNodes, List<ZSqlNode> defaultSqlNodes) {
            List<XNode> children = chooseSqlNode.getChildren();
            Iterator var5 = children.iterator();

            while(var5.hasNext()) {
                XNode child = (XNode)var5.next();
                String nodeName = child.getNode().getNodeName();
                ZXMLScriptBuilder.NodeHandler handler = (ZXMLScriptBuilder.NodeHandler)ZXMLScriptBuilder.this.nodeHandlerMap.get(nodeName);
                if (handler instanceof ZXMLScriptBuilder.IfHandler) {
                    handler.handleNode(child, ifSqlNodes);
                } else if (handler instanceof ZXMLScriptBuilder.OtherwiseHandler) {
                    handler.handleNode(child, defaultSqlNodes);
                }
            }

        }

        private ZSqlNode getDefaultSqlNode(List<ZSqlNode> defaultSqlNodes) {
            ZSqlNode defaultSqlNode = null;
            if (defaultSqlNodes.size() == 1) {
                defaultSqlNode = (ZSqlNode)defaultSqlNodes.get(0);
            } else if (defaultSqlNodes.size() > 1) {
                throw new BuilderException("Too many default (otherwise) elements in choose statement.");
            }

            return defaultSqlNode;
        }
    }

    private class OtherwiseHandler implements ZXMLScriptBuilder.NodeHandler {
        public OtherwiseHandler() {
        }

        public void handleNode(XNode nodeToHandle, List<ZSqlNode> targetContents) {
            ZMixedSqlNode mixedSqlNode = ZXMLScriptBuilder.this.parseDynamicTags(nodeToHandle);
            targetContents.add(mixedSqlNode);
        }
    }

    private class IfHandler implements ZXMLScriptBuilder.NodeHandler {
        public IfHandler() {
        }

        public void handleNode(XNode nodeToHandle, List<ZSqlNode> targetContents) {
            ZMixedSqlNode mixedSqlNode = ZXMLScriptBuilder.this.parseDynamicTags(nodeToHandle);
            String test = nodeToHandle.getStringAttribute("test");
            ZIfSqlNode ifSqlNode = new ZIfSqlNode(mixedSqlNode, test);
            targetContents.add(ifSqlNode);
        }
    }

    private class ZForEachHandler implements ZXMLScriptBuilder.NodeHandler {
        public ZForEachHandler() {
        }

        public void handleNode(XNode nodeToHandle, List<ZSqlNode> targetContents) {
            ZMixedSqlNode mixedSqlNode = ZXMLScriptBuilder.this.parseDynamicTags(nodeToHandle);
            String collection = nodeToHandle.getStringAttribute("collection");
            String item = nodeToHandle.getStringAttribute("item");
            String index = nodeToHandle.getStringAttribute("index");
            String open = nodeToHandle.getStringAttribute("open");
            String close = nodeToHandle.getStringAttribute("close");
            String separator = nodeToHandle.getStringAttribute("separator");
            ZForEachSqlNode forEachSqlNode = new ZForEachSqlNode(ZXMLScriptBuilder.this.configuration, mixedSqlNode, collection, index, item, open, close, separator);
            targetContents.add(forEachSqlNode);
        }
    }

    private class ZSetHandler implements ZXMLScriptBuilder.NodeHandler {
        public ZSetHandler() {
        }

        public void handleNode(XNode nodeToHandle, List<ZSqlNode> targetContents) {
            ZMixedSqlNode mixedSqlNode = ZXMLScriptBuilder.this.parseDynamicTags(nodeToHandle);
            ZSetSqlNode set = new ZSetSqlNode(ZXMLScriptBuilder.this.configuration, mixedSqlNode);
            targetContents.add(set);
        }
    }

    private class ZWhereHandler implements ZXMLScriptBuilder.NodeHandler {
        public ZWhereHandler() {
        }

        public void handleNode(XNode nodeToHandle, List<ZSqlNode> targetContents) {
            ZMixedSqlNode mixedSqlNode = ZXMLScriptBuilder.this.parseDynamicTags(nodeToHandle);
            ZWhereSqlNode where = new ZWhereSqlNode(ZXMLScriptBuilder.this.configuration, mixedSqlNode);
            targetContents.add(where);
        }
    }

    private class ZTrimHandler implements ZXMLScriptBuilder.NodeHandler {
        public ZTrimHandler() {
        }

        public void handleNode(XNode nodeToHandle, List<ZSqlNode> targetContents) {
            ZMixedSqlNode mixedSqlNode = ZXMLScriptBuilder.this.parseDynamicTags(nodeToHandle);
            String prefix = nodeToHandle.getStringAttribute("prefix");
            String prefixOverrides = nodeToHandle.getStringAttribute("prefixOverrides");
            String suffix = nodeToHandle.getStringAttribute("suffix");
            String suffixOverrides = nodeToHandle.getStringAttribute("suffixOverrides");
            ZTrimSqlNode trim = new ZTrimSqlNode(ZXMLScriptBuilder.this.configuration, mixedSqlNode, prefix, prefixOverrides, suffix, suffixOverrides);
            targetContents.add(trim);
        }
    }

    private class BindHandler implements ZXMLScriptBuilder.NodeHandler {
        public BindHandler() {
        }

        public void handleNode(XNode nodeToHandle, List<ZSqlNode> targetContents) {
            String name = nodeToHandle.getStringAttribute("name");
            String expression = nodeToHandle.getStringAttribute("value");
            ZVarDeclSqlNode node = new ZVarDeclSqlNode(name, expression);
            targetContents.add(node);
        }
    }

    private interface NodeHandler {
        void handleNode(XNode var1, List<ZSqlNode> var2);
    }
}
