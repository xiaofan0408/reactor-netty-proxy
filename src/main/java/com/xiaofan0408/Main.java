package com.xiaofan0408;


import com.xiaofan0408.config.ProxyConfig;

/**
 * @author: xuzefan
 * @date: 2023/5/11 16:33
 */
public class Main {

    public static void main(String[] args) {
        ProxyConfig proxyConfig = ProxyConfig.builder()
                .port(10888)
                .proxyType("http")
                .wiretap(true)
                .build();

        ReactorProxyServer reactorProxyServer = new ReactorProxyServer(proxyConfig);
        reactorProxyServer.start();
        reactorProxyServer.listen();
    }

}
