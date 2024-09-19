package com.zmy.base.scripting.defaults;

import com.zmy.base.scripting.xmltags.ZXMLLanguageDriver;
import com.zmy.core.session.ZConfiguration;
import com.zmy.core.mapping.ZSqlSource;
import org.apache.ibatis.builder.BuilderException;
import org.apache.ibatis.parsing.XNode;
import org.apache.ibatis.scripting.defaults.RawSqlSource;

public class ZRawLanguageDriver extends ZXMLLanguageDriver {

    public ZRawLanguageDriver() {
    }

    public ZSqlSource createSqlSource(ZConfiguration configuration, XNode script, Class<?> parameterType) {
        ZSqlSource source = super.createSqlSource(configuration, script, parameterType);
        this.checkIsNotDynamic(source);
        return source;
    }

    public ZSqlSource createSqlSource(ZConfiguration configuration, String script, Class<?> parameterType) {
        ZSqlSource source = super.createSqlSource(configuration, script, parameterType);
        this.checkIsNotDynamic(source);
        return source;
    }

    /**
     * TODO
     * @param source
     */
    private void checkIsNotDynamic(ZSqlSource source) {
        if (!RawSqlSource.class.equals(source.getClass())) {
            throw new BuilderException("Dynamic content is not allowed when using RAW language");
        }
    }
}
