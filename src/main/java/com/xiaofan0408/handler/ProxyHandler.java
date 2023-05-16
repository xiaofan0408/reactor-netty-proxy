package com.xiaofan0408.handler;

import reactor.context.ProxyCtx;

public interface ProxyHandler {

    void doHandle(ProxyCtx ctx);

}
