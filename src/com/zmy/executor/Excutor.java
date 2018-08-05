package com.zmy.executor;

public interface Excutor {

    public <T> T query(String statement, String parameter);
}
