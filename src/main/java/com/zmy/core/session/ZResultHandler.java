package com.zmy.core.session;

import org.apache.ibatis.session.ResultContext;

public interface ZResultHandler<T> {

    void handleResult(ResultContext<? extends T> resultContext);
}
