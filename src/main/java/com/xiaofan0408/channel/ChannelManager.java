package com.xiaofan0408.channel;

import reactor.connection.ConnectionEx;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: xuzefan
 * @date: 2023/5/12 9:44
 */
public class ChannelManager {

    private static Map<String, ConnectionEx> connectionMap = new ConcurrentHashMap<>();

    public static void put(String key,ConnectionEx connection){
        connectionMap.put(key,connection);
    }

    public static void remove(InetSocketAddress socketAddress) {
        connectionMap.remove(socketAddress.toString());
    }

    public ConnectionEx get(String key){
        return connectionMap.get(key);
    }
}