package com.zmy.base.builder;

import com.zmy.core.mapping.ZDiscriminator;
import com.zmy.core.mapping.ZResultMap;
import com.zmy.core.mapping.ZResultMapping;
import org.apache.ibatis.mapping.Discriminator;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.ResultMapping;

import java.util.List;

public class ZResultMapResolver {
    private final ZMapperBuilderAssistant assistant;
    private final String id;
    private final Class<?> type;
    private final String extend;
    private final ZDiscriminator discriminator;
    private final List<ZResultMapping> resultMappings;
    private final Boolean autoMapping;


    public ZResultMapResolver(ZMapperBuilderAssistant assistant, String id, Class<?> type, String extend, ZDiscriminator discriminator, List<ZResultMapping> resultMappings, Boolean autoMapping) {
        this.assistant = assistant;
        this.id = id;
        this.type = type;
        this.extend = extend;
        this.discriminator = discriminator;
        this.resultMappings = resultMappings;
        this.autoMapping = autoMapping;
    }

    public ZResultMap resolve() {
        return assistant.addResultMap(this.id, this.type, this.extend, this.discriminator, this.resultMappings, this.autoMapping);
    }

}
