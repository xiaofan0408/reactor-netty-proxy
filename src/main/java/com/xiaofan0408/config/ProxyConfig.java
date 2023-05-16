package com.xiaofan0408.config;

import lombok.Builder;
import lombok.Data;

/**
 * @author: xuzefan
 * @date: 2023/5/16 16:08
 */

@Data
@Builder
public class ProxyConfig {

    private String proxyType;

    private Integer port;

    private boolean wiretap;

}
