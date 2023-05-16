package com.xiaofan0408;


import com.xiaofan0408.channel.ChannelManager;
import com.xiaofan0408.config.ProxyConfig;
import com.xiaofan0408.connection.ConnectionEx;
import com.xiaofan0408.handler.HttpsProxyHandler;
import com.xiaofan0408.handler.ProxyHandler;
import com.xiaofan0408.handler.Socks5ProxyHandler;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import reactor.core.Disposable;
import reactor.netty.*;
import reactor.netty.tcp.TcpServer;

import java.net.InetSocketAddress;
import java.util.Objects;

/**
 * @author: xuzefan
 * @date: 2023/5/11 16:33
 */

@Slf4j
public class ReactorProxyServer implements Server {

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

    private ProxyConfig proxyConfig;

    /**
     * Constructs a proxy server redirecting the received port to the target URL.
     *
     * @param prt
     * port to listen to
     */
    public ReactorProxyServer(ProxyConfig proxyConfig) {
        super();

        port = Objects.requireNonNull(proxyConfig.getPort());

        wiretap = proxyConfig.isWiretap();
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
                // add connection
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
        ProxyHandler proxyHandler = null;
        if (proxyConfig.getProxyType().equals("socks")) {
            proxyHandler = new Socks5ProxyHandler();
        } else if (proxyConfig.getProxyType().equals("http")){
            proxyHandler = new HttpsProxyHandler();
        }

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
