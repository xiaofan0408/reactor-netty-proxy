package com.xiaofan0408.context;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import reactor.core.publisher.Sinks;
import reactor.netty.Connection;
import reactor.netty.NettyInbound;
import reactor.netty.NettyOutbound;

/**
 * @author: xuzefan
 * @date: 2023/5/16 10:34
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProxyCtx {

    private Sinks.Many<ByteBuf> requests;

    private Connection connection;

    private NettyInbound nettyInbound;

    private NettyOutbound nettyOutbound;

}