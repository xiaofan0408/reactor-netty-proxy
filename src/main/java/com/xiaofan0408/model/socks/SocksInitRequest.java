package com.xiaofan0408.model.socks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class SocksInitRequest {
    private final static Logger logger = LoggerFactory.getLogger(SocksInitRequest.class);

    private final byte[] data;

    private byte version;
    private byte NMETHODS;
    private byte[] METHODS;
    private SocksInitRequest(byte[] data) {
        this.data = data;
        this.version = data[0];
        this.NMETHODS = data[1];
        this.METHODS = Arrays.copyOfRange(data,2, data.length);
    }

    private static boolean validate(byte[] data) {
        if (data.length < 3) return false;
        if (data[0] != 0x05) return false;
        return true;
    }

    public static SocksInitRequest fromBytes(byte[] data) {
        if(validate(data)) {
            return new SocksInitRequest(data);
        } else {
            return null;
        }
    }
}
