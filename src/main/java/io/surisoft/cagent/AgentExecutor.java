package io.surisoft.cagent;

import com.google.gson.reflect.TypeToken;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1Service;
import io.kubernetes.client.util.Watch;
import io.surisoft.cagent.schema.AgentEnvironment;
import io.surisoft.cagent.service.ConsulService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AgentExecutor {
    private static final Logger logger = LoggerFactory.getLogger(AgentExecutor.class);
    List<String> services = new ArrayList<>();
    public void start(ApiClient apiClient, AgentEnvironment agentEnvironment, ConsulService consulService) {
        try (ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor()) {
            executorService.scheduleAtFixedRate(() -> {
                        try {
                            CoreV1Api coreV1Api = new CoreV1Api(apiClient);
                            try (Watch<V1Service> watch = Watch.createWatch(
                                    apiClient,
                                    coreV1Api.listServiceForAllNamespacesCall(false, null, null, null, 100, "false",
                                            null, null, false, 10, true, null),
                                    new TypeToken<Watch.Response<V1Service>>() {
                                    }.getType())) {
                                for (Watch.Response<V1Service> event : watch) {
                                    V1ObjectMeta serviceMetadata = event.object.getMetadata();
                                    if (event.type.equals("ADDED") && !services.contains(Objects.requireNonNull(event.object.getMetadata()).getName())) {
                                        if (registerService(serviceMetadata, agentEnvironment)) {
                                            logger.info("New service {} detected, adding to the list", event.object.getMetadata().getName());
                                            services.add(event.object.getMetadata().getName());
                                        }
                                    } else if (event.type.equals("DELETED")) {
                                        if (registerService(serviceMetadata, agentEnvironment)) {
                                            logger.info("Deleted service {} detected, removing from list", Objects.requireNonNull(event.object.getMetadata()).getName());
                                            services.remove(event.object.getMetadata().getName());
                                        }
                                    }
                                }
                            } catch (ApiException e) {
                                logger.error(e.getMessage(), e);
                            }
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                        }
                    }, agentEnvironment.getExecutorInitialDelay(),
                    agentEnvironment.getExecutorExecutionInterval(),
                    TimeUnit.SECONDS);
        }
    }

    private boolean registerService(V1ObjectMeta objectMeta, AgentEnvironment agentEnvironment) {
        for(String label : agentEnvironment.getLabelsToFilter()) {
            if(Objects.requireNonNull(objectMeta.getLabels()).containsKey(label)) {
                return true;
            }
        }
        return false;
    }
}