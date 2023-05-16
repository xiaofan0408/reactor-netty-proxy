package com.xiaofan0408;

import reactor.server.ReactorProxyServer;

/**
 * @author: xuzefan
 * @date: 2023/5/11 16:33
 */
public class Main {

    public static void main(String[] args) {
        ReactorProxyServer reactorProxyServer = new ReactorProxyServer(443);
        reactorProxyServer.start();
        reactorProxyServer.listen();
    }

}
