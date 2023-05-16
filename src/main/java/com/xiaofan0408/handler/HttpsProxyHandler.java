package com.xiaofan0408.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import reactor.channel.ClientManager;
import reactor.client.ReactorNettyProxyClient;
import reactor.context.ProxyCtx;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.netty.Connection;
import reactor.netty.NettyInbound;
import reactor.netty.NettyOutbound;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @author: xuzefan
 * @date: 2023/5/16 10:37
 */
public class HttpsProxyHandler implements ProxyHandler{
    @Override
    public void doHandle(ProxyCtx ctx) {
        NettyInbound inbound = ctx.getNettyInbound();
        NettyOutbound outbound = ctx.getNettyOutbound();
        Connection connection = ctx.getConnection();
        Sinks.Many<ByteBuf> requests = ctx.getRequests();

        inbound.receive().subscribe(byteBuf -> {
            if (ClientManager.contains(connection.toString())) {
                ByteBuf copy = byteBuf.copy();
                Connection clientConn = ClientManager.get(connection.toString());
                clientConn.outbound().send(Mono.just(copy)).then().subscribe();
            } else {
                String data = byteBuf.toString(Charset.defaultCharset());
                System.out.println(data);
                if (data.startsWith("CONNECT")) {
                    String[] arr = data.split("\r\n");
                    String host = arr[1];
                    host = host.replace("Host: ", "");
                    host = host.replace(":443", "");
                    ReactorNettyProxyClient client = new ReactorNettyProxyClient(host, 443);
                    client.connect().subscribe(conn -> {
                        ClientManager.put(connection.toString(), conn);
                        requests.asFlux().concatMap(bf -> {
                            return outbound.sendObject(bf);
                        }).subscribe();

                        conn.inbound().receive().subscribe(b -> {
                            ByteBuf b2 = b.copy();
                            requests.tryEmitNext(b2);
                        });
                    });
                    String resp = "HTTP/1.1 200 Connection Established\r\n\r\n";
                    byte[] respArr = resp.getBytes(StandardCharsets.UTF_8);
                    ByteBuf bf = Unpooled.wrappedBuffer(respArr);
                    requests.tryEmitNext(bf);
                } else if (data.startsWith("GET")||data.startsWith("POST")||data.startsWith("PUT")) {
                    String[] arr = data.split("\r\n");
                    String host = arr[1];
                    host = host.replace("Host: ", "");
                    ReactorNettyProxyClient client = new ReactorNettyProxyClient(host, 80);
                    client.connect().subscribe(conn -> {
                        ClientManager.put(connection.toString(), conn);
                        requests.asFlux().concatMap(bf -> {
                            return outbound.sendObject(bf);
                        }).subscribe();

                        conn.inbound().receive().subscribe(b -> {
                            ByteBuf b2 = b.copy();
                            requests.tryEmitNext(b2);
                        });
                        byte[] reqArr = data.getBytes(StandardCharsets.UTF_8);
                        ByteBuf bf = Unpooled.wrappedBuffer(reqArr);
                        conn.outbound().send(Mono.just(bf)).then().subscribe();
                    });
                }
            }
        });
    }
}
