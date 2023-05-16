package com.xiaofan0408.handler;

import com.xiaofan0408.client.ReactorNettyProxyClient;
import com.xiaofan0408.context.ProxyCtx;
import com.xiaofan0408.handler.ProxyHandler;
import com.xiaofan0408.model.socks.SocksConnectionRequest;
import com.xiaofan0408.model.socks.SocksConnectionResponse;
import com.xiaofan0408.model.socks.SocksInitRequest;
import com.xiaofan0408.model.socks.SocksInitResponse;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.netty.Connection;
import reactor.netty.NettyInbound;
import reactor.netty.NettyOutbound;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: xuzefan
 * @date: 2023/5/16 10:37
 */
public class Socks5ProxyHandler implements ProxyHandler {

    private boolean isInit = false;

    private static final int CONNECT = 1;

    private static final int INIT = 2;

    private static final int SEND = 3;

    private int status = CONNECT;

    private Map<String,Connection> clients = new HashMap<>();

    @Override
    public void doHandle(ProxyCtx ctx) {
        NettyInbound inbound = ctx.getNettyInbound();
        NettyOutbound outbound = ctx.getNettyOutbound();
        Connection connection = ctx.getConnection();
        Sinks.Many<ByteBuf> requests = ctx.getRequests();

        inbound.receive().subscribe(byteBuf -> {
            byte[] arr = ByteBufUtil.getBytes(byteBuf);
            if (status == INIT) {
                SocksConnectionRequest socksConnectionRequest = SocksConnectionRequest.fromBytes(arr);
                String addr = socksConnectionRequest.getAddress();
                int port =socksConnectionRequest.getDstPort();
                ReactorNettyProxyClient client = new ReactorNettyProxyClient(addr,port);
                client.connect().subscribe(conn -> {
                    clients.put(connection.toString(),conn);
                    byte[] socksInitResponse = SocksConnectionResponse.responseBytes();
                    ByteBuf resp = Unpooled.wrappedBuffer(socksInitResponse);
                    requests.tryEmitNext(resp);
                    conn.inbound().receive().subscribe(b -> {
                        ByteBuf b2 = b.copy();
                        requests.tryEmitNext(b2);
                    });
                });
                status = SEND;
            } else if (status == CONNECT) {
                SocksInitRequest socksInitRequest = SocksInitRequest.fromBytes(arr);
                byte[] socksInitResponse = SocksInitResponse.responseBytes();
                ByteBuf resp = Unpooled.wrappedBuffer(socksInitResponse);
                requests.tryEmitNext(resp);
                requests.asFlux().concatMap(bf -> {
                    return outbound.sendObject(bf);
                }).subscribe();
                status = INIT;
            } else if (status == SEND) {
                ByteBuf copy = Unpooled.wrappedBuffer(arr);
                Connection clientConn = clients.get(connection.toString());
                clientConn.outbound().send(Mono.just(copy)).then().subscribe();
            }
        });
    }
}
