package com.zmy.core.mapping;

import com.zmy.core.session.ZConfiguration;

import java.util.Collections;
import java.util.Map;

public class ZDiscriminator {
    private ZResultMapping resultMapping;
    private Map<String, String> discriminatorMap;

    ZDiscriminator() {
    }

    public ZResultMapping getResultMapping() {
        return this.resultMapping;
    }

    public Map<String, String> getDiscriminatorMap() {
        return this.discriminatorMap;
    }

    public String getMapIdFor(String s) {
        return (String)this.discriminatorMap.get(s);
    }

    public static class Builder {
        private ZDiscriminator discriminator = new ZDiscriminator();

        public Builder(ZConfiguration configuration, ZResultMapping resultMapping, Map<String, String> discriminatorMap) {
            this.discriminator.resultMapping = resultMapping;
            this.discriminator.discriminatorMap = discriminatorMap;
        }

        public ZDiscriminator build() {
            assert this.discriminator.resultMapping != null;

            assert this.discriminator.discriminatorMap != null;

            assert !this.discriminator.discriminatorMap.isEmpty();

            this.discriminator.discriminatorMap = Collections.unmodifiableMap(this.discriminator.discriminatorMap);
            return this.discriminator;
        }
    }
}
