package com.xiaofan0408.client;

import reactor.core.publisher.Mono;
import reactor.netty.Connection;

/**
 * Simple client which just attempts to connect, returning a mono for listening for the actual connection event.
 *
 * @author Bernardo Mart&iacute;nez Garrido
 *
 */
public interface Client {

    /**
     * Create and return a new connection. Said connection will come in a {@code Mono}, to allow subscribing
     * asynchronously.
     *
     * @return a {@code Mono} for the client connection
     */
    Mono<? extends Connection> connect();

}