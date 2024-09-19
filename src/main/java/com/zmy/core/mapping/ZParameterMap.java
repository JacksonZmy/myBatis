package com.zmy.core.mapping;

import com.zmy.core.session.ZConfiguration;
import java.util.Collections;
import java.util.List;

public class ZParameterMap {

    private String id;
    private Class<?> type;
    private List<ZParameterMapping> parameterMappings;

    private ZParameterMap() {
    }

    public static class Builder {
        private ZParameterMap parameterMap = new ZParameterMap();

        public Builder(ZConfiguration configuration, String id, Class<?> type, List<ZParameterMapping> parameterMappings) {
            parameterMap.id = id;
            parameterMap.type = type;
            parameterMap.parameterMappings = parameterMappings;
        }

        public Class<?> type() {
            return parameterMap.type;
        }

        public ZParameterMap build() {
            //lock down collections
            parameterMap.parameterMappings = Collections.unmodifiableList(parameterMap.parameterMappings);
            return parameterMap;
        }
    }

    public String getId() {
        return id;
    }

    public Class<?> getType() {
        return type;
    }

    public List<ZParameterMapping> getParameterMappings() {
        return parameterMappings;
    }
}
