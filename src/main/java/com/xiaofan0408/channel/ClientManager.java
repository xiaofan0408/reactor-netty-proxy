package com.xiaofan0408.channel;

import reactor.netty.Connection;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: xuzefan
 * @date: 2023/5/12 10:19
 */
public class ClientManager {

    private static Map<String, Connection> connectionMap = new ConcurrentHashMap<>();

    public static void put(String key,Connection connection){
        connectionMap.put(key,connection);
    }

    public static Connection get(String key){
        return connectionMap.get(key);
    }

    public static boolean contains(String key) {
        return connectionMap.containsKey(key);
    }
}
