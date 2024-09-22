package com.zmy.base.builder;

import org.apache.ibatis.cache.Cache;

public class ZCacheRefResolver {

    private final ZMapperBuilderAssistant assistant;
    private final String cacheRefNamespace;

    public ZCacheRefResolver(ZMapperBuilderAssistant assistant, String cacheRefNamespace) {
        this.assistant = assistant;
        this.cacheRefNamespace = cacheRefNamespace;
    }

    public Cache resolveCacheRef() {
        return this.assistant.useCacheRef(this.cacheRefNamespace);
    }
}
