package com.xiaofan0408;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import reactor.bridge.ConnectionBridge;
import reactor.bridge.ProxyConnectionBridge;
import reactor.channel.ChannelManager;
import reactor.connection.ConnectionEx;
import reactor.core.Disposable;
import reactor.handler.HttpsProxyHandler;
import reactor.handler.ProxyHandler;
import reactor.handler.Socks5ProxyHandler;
import reactor.netty.*;
import reactor.netty.tcp.TcpServer;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.function.Function;

/**
 * @author: xuzefan
 * @date: 2023/5/11 16:33
 */

@Slf4j
public class ReactorProxyServer implements Server {

    /**
     * Connection bridge to connect the proxy server and clients.
     */
    private final ConnectionBridge bridge;

    /**
     * Port which the server will listen to.
     */
    private final Integer  port;

    /**
     * Disposable for closing the server port connection.
     */
    private DisposableChannel server;

    /**
     * Wiretap flag. Activates Reactor Netty wiretap logging.
     */
    @NonNull
    private Boolean wiretap = false;

    /**
     * Constructs a proxy server redirecting the received port to the target URL.
     *
     * @param prt
     * port to listen to
     */
    public ReactorProxyServer(final Integer prt) {
        super();

        port = Objects.requireNonNull(prt);

        bridge = new ProxyConnectionBridge();
    }

    @Override
    public final void listen() {
        log.trace("Starting server listening");

        server.onDispose()
                .block();

        log.trace("Stopped server listening");
    }

    public final void setWiretap(final Boolean flag) {
        wiretap = flag;
    }

    @Override
    public final void start() {
        log.trace("Starting server");

        log.debug("Binding to port {}", port);

        server = TcpServer.create()
                // Bridge connection
                .doOnConnection(this::addConnections)
                // Listen to events
//                .doOnBind(c -> listener.onStart())
                // Wiretap
                .wiretap(wiretap)
                // Bind to port
                .port(port)
                .bindNow()
                // Listen to events
                .onDispose(new Disposable() {
                    @Override
                    public void dispose() {
                    }
                });

        log.trace("Started server");
    }

    @Override
    public final void stop() {
        log.trace("Stopping server");

        server.dispose();

        log.trace("Stopped server");
    }


    /**
     * Bridges the server and client connections.
     *
     * @param serverConn
     *            server connection
     */
    private final void addConnections(final Connection serverConn) {
        log.debug("add connection");
        ProxyHandler proxyHandler = new HttpsProxyHandler();
        InetSocketAddress socketAddress = (InetSocketAddress)serverConn.channel().remoteAddress();
        ConnectionEx connectionEx = new ConnectionEx(serverConn,proxyHandler);
        ChannelManager.put(socketAddress.toString(),connectionEx);
       serverConn.channel().closeFuture().addListener(future -> {
          if (future.isSuccess()) {
              log.info("close: {}", socketAddress);
              ChannelManager.remove(socketAddress);
              connectionEx.close();
          }
       });
    }
}
