package com.xiaofan0408.model.socks;

public class SocksInitResponse {

    /*
     a common success response
     */
    public static byte[] responseBytes() {
        byte[] response = new byte[2];
        response[0] = 0x05;
        response[1] = 0x00;
        return response;
    }
}