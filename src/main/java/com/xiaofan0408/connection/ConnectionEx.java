package com.xiaofan0408.connection;

import com.xiaofan0408.context.ProxyCtx;
import com.xiaofan0408.handler.ProxyHandler;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import reactor.core.publisher.*;
import reactor.netty.Connection;
import reactor.netty.NettyInbound;
import reactor.netty.NettyOutbound;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

public class ConnectionEx {

    private static final Logger logger = Loggers.getLogger(ConnectionEx.class);

    private Connection connection;

    private final AtomicBoolean isClosed = new AtomicBoolean(false);

    private Sinks.Many<ByteBuf> requests = Sinks.many().unicast().onBackpressureBuffer();

    private ProxyHandler proxyHandler;

    public ConnectionEx(Connection connection,ProxyHandler proxyHandler) {
        this.connection = connection;
        this.proxyHandler = proxyHandler;
        if (logger.isTraceEnabled()) {
            connection.addHandlerFirst(
                    LoggingHandler.class.getSimpleName(),
                    new LoggingHandler(ConnectionEx.class, LogLevel.TRACE));
        }
        ProxyCtx proxyCtx = new ProxyCtx(requests,connection,connection.inbound(),connection.outbound());
        proxyHandler.doHandle(proxyCtx);
    }


    private void doCancel(){

    }

    public Mono<Void> close() {
        return Mono.defer(
            () -> {
                if (this.isClosed.compareAndSet(false, true)) {

                    Channel channel = this.connection.channel();
                    if (!channel.isOpen()) {
                        this.connection.dispose();
                        return this.connection.onDispose();
                    }
                }
                return Mono.empty();
            });
    }

    private void handleConnectionError(Throwable throwable) {

    }


    @Override
    public String toString() {
        return "Client{isClosed=" + isClosed + '}';
    }



    private void handleIncomingFrames(ByteBuf frame) {
    }

}
