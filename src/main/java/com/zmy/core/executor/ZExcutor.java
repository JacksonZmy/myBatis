package com.zmy.core.executor;

public interface ZExcutor {

    public <T> T query(String statement, String parameter);
}
