package com.zmy.base.scripting.xmltags;

import com.zmy.base.scripting.ZLanguageDriver;
import com.zmy.base.scripting.defaults.ZDefaultParameterHandler;
import com.zmy.base.scripting.defaults.ZRawSqlSource;
import com.zmy.core.executor.parameter.ZParameterHandler;
import com.zmy.core.session.ZConfiguration;
import com.zmy.core.mapping.ZBoundSql;
import com.zmy.core.mapping.ZMappedStatement;
import com.zmy.core.mapping.ZSqlSource;
import org.apache.ibatis.builder.xml.XMLMapperEntityResolver;
import org.apache.ibatis.parsing.PropertyParser;
import org.apache.ibatis.parsing.XNode;
import org.apache.ibatis.parsing.XPathParser;

public class ZXMLLanguageDriver implements ZLanguageDriver {
    @Override
    public ZParameterHandler createParameterHandler(ZMappedStatement mappedStatement, Object parameterObject, ZBoundSql boundSql) {
        return new ZDefaultParameterHandler(mappedStatement, parameterObject, boundSql);
    }

    public ZSqlSource createSqlSource(ZConfiguration configuration, XNode script, Class<?> parameterType) {
        ZXMLScriptBuilder builder = new ZXMLScriptBuilder(configuration, script, parameterType);
        return builder.parseScriptNode();
    }

    @Override
    public ZSqlSource createSqlSource(ZConfiguration configuration, String script, Class<?> parameterType) {
        if (script.startsWith("<script>")) {
            XPathParser parser = new XPathParser(script, false, configuration.getVariables(), new XMLMapperEntityResolver());
            return createSqlSource(configuration, parser.evalNode("/script"), parameterType);
        } else {
            // issue #127
            script = PropertyParser.parse(script, configuration.getVariables());
            ZTextSqlNode textSqlNode = new ZTextSqlNode(script);
            if (textSqlNode.isDynamic()) {
                return new ZDynamicSqlSource(configuration, textSqlNode);
            } else {
                return new ZRawSqlSource(configuration, script, parameterType);
            }
        }
    }
}
