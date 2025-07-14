package io.surisoft.cagent.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.surisoft.cagent.schema.Ingress;
import io.surisoft.cagent.schema.Meta;
import io.surisoft.cagent.schema.Service;
import io.surisoft.cagent.utils.Constants;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.UUID;

public class ConsulService {
    private static final Logger logger = LoggerFactory.getLogger(ConsulService.class);
    private final String host;
    private final String token;
    private final OkHttpClient okHttpClient;

    public ConsulService(String host, String token, OkHttpClient okHttpClient) {
        this.host = host;
        this.token = token;
        this.okHttpClient = okHttpClient;
    }

    public void registerService(Service service) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            RequestBody body = RequestBody.create(objectMapper.writeValueAsString(service), MediaType.parse(Constants.APPLICATION_JSON));
            Request.Builder requestBuilder = new Request.Builder()
                    .url(host + Constants.CONSUL_REGISTER_PATH)
                    .put(body);
            requestBuilder.addHeader(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON);
            if(token != null && !token.isEmpty()) {
                requestBuilder.addHeader(Constants.AUTHORIZATION_HEADER, Constants.BEARER_TOKEN + token);
            }
            Request request = requestBuilder.build();
            Call call = okHttpClient.newCall(request);
            try (Response response = call.execute()) {
                if(response.code() == 200) {
                    logger.info("Service {} registered with success on Consul.", service.getId());
                    service.setRegistered(true);
                }
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void registerIngress(Ingress ingress) {
        try {
            for(Meta meta : ingress.getMetaList()) {
                ObjectMapper objectMapper = new ObjectMapper();
                ingress.setId(ingress.getName() + "-" + ingress.getMetaList().getFirst().getGroup() + "-" + meta.getCapiInstance());
                ingress.setMeta(meta);
                RequestBody body = RequestBody.create(objectMapper.writeValueAsString(ingress), MediaType.parse(Constants.APPLICATION_JSON));
                logger.debug(objectMapper.writeValueAsString(ingress));
                Request.Builder requestBuilder = new Request.Builder()
                        .url(host + Constants.CONSUL_REGISTER_PATH)
                        .put(body);
                requestBuilder.addHeader(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON);
                if(token != null && !token.isEmpty()) {
                    requestBuilder.addHeader(Constants.AUTHORIZATION_HEADER, Constants.BEARER_TOKEN + token);
                }
                Request request = requestBuilder.build();
                Call call = okHttpClient.newCall(request);
                try (Response response = call.execute()) {
                    logger.debug(response.code()+"");
                    if(response.code() == 200) {
                        logger.debug("Ingress {} registered with success on Consul.", ingress.getId());
                        ingress.setRegistered(true);
                    }
                }
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void deregisterService(Service service) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            RequestBody body = RequestBody.create(objectMapper.writeValueAsString(service), MediaType.parse(Constants.APPLICATION_JSON));
            Request.Builder requestBuilder = new Request.Builder()
                    .url(host + Constants.CONSUL_DEREGISTER_PATH + service.getId())
                    .put(body);
            requestBuilder.addHeader(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON);
            if(token != null && !token.isEmpty()) {
                requestBuilder.addHeader(Constants.AUTHORIZATION_HEADER, Constants.BEARER_TOKEN + token);
            }
            Request request = requestBuilder.build();
            Call call = okHttpClient.newCall(request);
            try (Response response = call.execute()) {
                if(response.code() == 200) {
                    logger.info("Service {} removed with success from Consul.", service.getId());
                    service.setRegistered(true);
                }
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void deregisterIngress(Ingress ingress) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            RequestBody body = RequestBody.create(objectMapper.writeValueAsString(ingress), MediaType.parse(Constants.APPLICATION_JSON));
            Request.Builder requestBuilder = new Request.Builder()
                    .url(host + Constants.CONSUL_DEREGISTER_PATH + ingress.getId())
                    .put(body);
            requestBuilder.addHeader(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON);
            if(token != null && !token.isEmpty()) {
                requestBuilder.addHeader(Constants.AUTHORIZATION_HEADER, Constants.BEARER_TOKEN + token);
            }
            Request request = requestBuilder.build();
            Call call = okHttpClient.newCall(request);
            try (Response response = call.execute()) {
                if(response.code() == 200) {
                    logger.info("Ingress {} removed with success from Consul.", ingress.getId());
                    ingress.setRegistered(true);
                }
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }
}
