package com.zmy.core.executor;

import com.zmy.core.session.ZConfiguration;
import org.apache.ibatis.executor.ExecutorException;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.factory.ObjectFactory;

import java.lang.reflect.Array;
import java.util.List;

public class ZResultExtractor {
    private final ZConfiguration configuration;
    private final ObjectFactory objectFactory;

    public ZResultExtractor(ZConfiguration configuration, ObjectFactory objectFactory) {
        this.configuration = configuration;
        this.objectFactory = objectFactory;
    }

    public Object extractObjectFromList(List<Object> list, Class<?> targetType) {
        Object value = null;
        if (targetType != null && targetType.isAssignableFrom(list.getClass())) {
            value = list;
        } else if (targetType != null && this.objectFactory.isCollection(targetType)) {
            value = this.objectFactory.create(targetType);
            MetaObject metaObject = this.configuration.newMetaObject(value);
            metaObject.addAll(list);
        } else if (targetType != null && targetType.isArray()) {
            Class<?> arrayComponentType = targetType.getComponentType();
            Object array = Array.newInstance(arrayComponentType, list.size());
            if (arrayComponentType.isPrimitive()) {
                for(int i = 0; i < list.size(); ++i) {
                    Array.set(array, i, list.get(i));
                }

                value = array;
            } else {
                value = list.toArray((Object[])((Object[])array));
            }
        } else {
            if (list != null && list.size() > 1) {
                throw new ExecutorException("Statement returned more than one row, where no more than one was expected.");
            }

            if (list != null && list.size() == 1) {
                value = list.get(0);
            }
        }

        return value;
    }
}
