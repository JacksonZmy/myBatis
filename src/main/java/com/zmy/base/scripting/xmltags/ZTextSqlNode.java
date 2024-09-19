package com.zmy.base.scripting.xmltags;

import org.apache.ibatis.parsing.GenericTokenParser;
import org.apache.ibatis.parsing.TokenHandler;
import org.apache.ibatis.scripting.ScriptingException;
import org.apache.ibatis.scripting.xmltags.DynamicContext;
import org.apache.ibatis.scripting.xmltags.OgnlCache;
import org.apache.ibatis.scripting.xmltags.TextSqlNode;
import org.apache.ibatis.type.SimpleTypeRegistry;

import java.util.regex.Pattern;

public class ZTextSqlNode implements ZSqlNode{
    private final String text;
    private final Pattern injectionFilter;

    public ZTextSqlNode(String text) {
        this(text, (Pattern)null);
    }

    public ZTextSqlNode(String text, Pattern injectionFilter) {
        this.text = text;
        this.injectionFilter = injectionFilter;
    }

    public boolean isDynamic() {
        ZTextSqlNode.DynamicCheckerTokenParser checker = new ZTextSqlNode.DynamicCheckerTokenParser();
        GenericTokenParser parser = this.createParser(checker);
        parser.parse(this.text);
        return checker.isDynamic();
    }

    public boolean apply(ZDynamicContext context) {
        GenericTokenParser parser = this.createParser(new ZTextSqlNode.BindingTokenParser(context, this.injectionFilter));
        context.appendSql(parser.parse(this.text));
        return true;
    }

    private GenericTokenParser createParser(TokenHandler handler) {
        return new GenericTokenParser("${", "}", handler);
    }

    private static class DynamicCheckerTokenParser implements TokenHandler {
        private boolean isDynamic;

        public DynamicCheckerTokenParser() {
        }

        public boolean isDynamic() {
            return this.isDynamic;
        }

        public String handleToken(String content) {
            this.isDynamic = true;
            return null;
        }
    }

    private static class BindingTokenParser implements TokenHandler {
        private ZDynamicContext context;
        private Pattern injectionFilter;

        public BindingTokenParser(ZDynamicContext context, Pattern injectionFilter) {
            this.context = context;
            this.injectionFilter = injectionFilter;
        }

        public String handleToken(String content) {
            Object parameter = this.context.getBindings().get("_parameter");
            if (parameter == null) {
                this.context.getBindings().put("value", (Object)null);
            } else if (SimpleTypeRegistry.isSimpleType(parameter.getClass())) {
                this.context.getBindings().put("value", parameter);
            }

            Object value = OgnlCache.getValue(content, this.context.getBindings());
            String srtValue = value == null ? "" : String.valueOf(value);
            this.checkInjection(srtValue);
            return srtValue;
        }

        private void checkInjection(String value) {
            if (this.injectionFilter != null && !this.injectionFilter.matcher(value).matches()) {
                throw new ScriptingException("Invalid input. Please conform to regex" + this.injectionFilter.pattern());
            }
        }
    }
}
