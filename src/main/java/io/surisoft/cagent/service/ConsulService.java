package io.surisoft.cagent.service;

import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsulService {
    private static final Logger logger = LoggerFactory.getLogger(ConsulService.class);
    private String host;
    private String token;
    private OkHttpClient okHttpClient;

    public ConsulService(String host, String token, OkHttpClient okHttpClient) {
        this.host = host;
        this.okHttpClient = okHttpClient;
    }
}
