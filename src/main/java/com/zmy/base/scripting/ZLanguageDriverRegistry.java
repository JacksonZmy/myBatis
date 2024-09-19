package com.zmy.base.scripting;

import org.apache.ibatis.scripting.ScriptingException;

import java.util.HashMap;
import java.util.Map;

public class ZLanguageDriverRegistry {

    private final Map<Class<? extends ZLanguageDriver>, ZLanguageDriver> LANGUAGE_DRIVER_MAP = new HashMap<>();

    private Class<? extends ZLanguageDriver> defaultDriverClass;

    public void register(Class<? extends ZLanguageDriver> cls) {
        if (cls == null) {
            throw new IllegalArgumentException("null is not a valid Language Driver");
        }
        LANGUAGE_DRIVER_MAP.computeIfAbsent(cls, k -> {
            try {
                return k.getDeclaredConstructor().newInstance();
            } catch (Exception ex) {
                throw new ScriptingException("Failed to load language driver for " + cls.getName(), ex);
            }
        });
    }

    public void register(ZLanguageDriver instance) {
        if (instance == null) {
            throw new IllegalArgumentException("null is not a valid Language Driver");
        }
        Class<? extends ZLanguageDriver> cls = instance.getClass();
        if (!LANGUAGE_DRIVER_MAP.containsKey(cls)) {
            LANGUAGE_DRIVER_MAP.put(cls, instance);
        }
    }

    public ZLanguageDriver getDriver(Class<? extends ZLanguageDriver> cls) {
        return LANGUAGE_DRIVER_MAP.get(cls);
    }

    public ZLanguageDriver getDefaultDriver() {
        return getDriver(getDefaultDriverClass());
    }

    public Class<? extends ZLanguageDriver> getDefaultDriverClass() {
        return defaultDriverClass;
    }

    public void setDefaultDriverClass(Class<? extends ZLanguageDriver> defaultDriverClass) {
        register(defaultDriverClass);
        this.defaultDriverClass = defaultDriverClass;
    }
}
