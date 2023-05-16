package com.xiaofan0408.client;

import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.netty.Connection;
import reactor.netty.tcp.TcpClient;

import java.util.Objects;

/**
 * Client for the proxy. This can create as many connections to the target server as needed. These are created
 * asynchronously, and returned inside a {@code Mono}.
 *
 * @author Bernardo Mart&iacute;nez Garrido
 *
 */
@Slf4j
public final class ReactorNettyProxyClient implements Client {

    /**
     * Host to which the proxy will connect.
     */
    private final String  host;

    /**
     * Port to which the proxy will connect.
     */
    private final Integer port;

    /**
     * Wiretap flag. Activates Reactor Netty wiretap logging.
     */
    @Setter
    @NonNull
    private Boolean  wiretap = false;

    /**
     * Constructs a client for the received host and port.
     *
     * @param hst
     *            host to connect to
     * @param prt
     *            port to connect to
     */
    public ReactorNettyProxyClient(final String hst, final Integer prt) {
        super();

        host = Objects.requireNonNull(hst);
        port = Objects.requireNonNull(prt);
    }

    @Override
    public final Mono<? extends Connection> connect() {
        log.trace("Starting proxy client");

        log.debug("Connecting to {}:{}", host, port);

        return TcpClient.create()
            // Wiretap
            .wiretap(wiretap)
            // Connect to target
            .host(host)
            .port(port)
            .connect();
    }

}
