package com.zmy.base.builder.xml;

import com.zmy.base.builder.ZMapperBuilderAssistant;
import com.zmy.core.session.ZConfiguration;
import org.apache.ibatis.builder.BuilderException;
import org.apache.ibatis.builder.IncompleteElementException;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.parsing.PropertyParser;
import org.apache.ibatis.parsing.XNode;
import org.apache.ibatis.session.Configuration;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

/**
 * 解析可复用的sql-->Fragments
 * 标签：incllude、refid、、
 */
public class ZXMLIncludeTransformer {
    private final ZConfiguration configuration;
    private final ZMapperBuilderAssistant builderAssistant;

    public ZXMLIncludeTransformer(ZConfiguration configuration, ZMapperBuilderAssistant builderAssistant) {
        this.configuration = configuration;
        this.builderAssistant = builderAssistant;
    }

    public void applyIncludes(Node source) {
        Properties variablesContext = new Properties();
        Properties configurationVariables = configuration.getVariables();
        Optional.ofNullable(configurationVariables).ifPresent(variablesContext::putAll);
        applyIncludes(source, variablesContext, false);
    }

    /**
     * 递归地应用所有 SQL 的 fragments.
     * @param source Include node in DOM tree
     * @param variablesContext Current context for static variables with values
     */
    private void applyIncludes(Node source, final Properties variablesContext, boolean included) {
        if (source.getNodeName().equals("include")) {
            Node toInclude = findSqlFragment(getStringAttribute(source, "refid"), variablesContext);
            Properties toIncludeContext = getVariablesContext(source, variablesContext);
            applyIncludes(toInclude, toIncludeContext, true);
            if (toInclude.getOwnerDocument() != source.getOwnerDocument()) {
                toInclude = source.getOwnerDocument().importNode(toInclude, true);
            }
            source.getParentNode().replaceChild(toInclude, source);
            while (toInclude.hasChildNodes()) {
                toInclude.getParentNode().insertBefore(toInclude.getFirstChild(), toInclude);
            }
            toInclude.getParentNode().removeChild(toInclude);
        } else if (source.getNodeType() == Node.ELEMENT_NODE) {
            if (included && !variablesContext.isEmpty()) {
                // replace variables in attribute values
                NamedNodeMap attributes = source.getAttributes();
                for (int i = 0; i < attributes.getLength(); i++) {
                    Node attr = attributes.item(i);
                    attr.setNodeValue(PropertyParser.parse(attr.getNodeValue(), variablesContext));
                }
            }
            NodeList children = source.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                applyIncludes(children.item(i), variablesContext, included);
            }
        } else if (included && (source.getNodeType() == Node.TEXT_NODE || source.getNodeType() == Node.CDATA_SECTION_NODE)
                && !variablesContext.isEmpty()) {
            // replace variables in text node
            source.setNodeValue(PropertyParser.parse(source.getNodeValue(), variablesContext));
        }
    }

    private Node findSqlFragment(String refid, Properties variables) {
        refid = PropertyParser.parse(refid, variables);
        refid = builderAssistant.applyCurrentNamespace(refid, true);
        try {
            XNode nodeToInclude = configuration.getSqlFragments().get(refid);
            return nodeToInclude.getNode().cloneNode(true);
        } catch (IllegalArgumentException e) {
            throw new IncompleteElementException("Could not find SQL statement to include with refid '" + refid + "'", e);
        }
    }

    private String getStringAttribute(Node node, String name) {
        return node.getAttributes().getNamedItem(name).getNodeValue();
    }

    /**
     * Read placeholders and their values from include node definition.
     * @param node Include node instance
     * @param inheritedVariablesContext Current context used for replace variables in new variables values
     * @return variables context from include instance (no inherited values)
     */
    private Properties getVariablesContext(Node node, Properties inheritedVariablesContext) {
        Map<String, String> declaredProperties = null;
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node n = children.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                String name = getStringAttribute(n, "name");
                // Replace variables inside
                String value = PropertyParser.parse(getStringAttribute(n, "value"), inheritedVariablesContext);
                if (declaredProperties == null) {
                    declaredProperties = new HashMap<>();
                }
                if (declaredProperties.put(name, value) != null) {
                    throw new BuilderException("Variable " + name + " defined twice in the same include definition");
                }
            }
        }
        if (declaredProperties == null) {
            return inheritedVariablesContext;
        } else {
            Properties newProperties = new Properties();
            newProperties.putAll(inheritedVariablesContext);
            newProperties.putAll(declaredProperties);
            return newProperties;
        }
    }
}
