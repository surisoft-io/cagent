package io.surisoft.cagent;

import com.google.gson.reflect.TypeToken;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Deployment;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1Service;
import io.kubernetes.client.util.Watch;
import io.surisoft.cagent.schema.AgentEnvironment;
import io.surisoft.cagent.schema.Service;
import io.surisoft.cagent.service.ConsulService;
import io.surisoft.cagent.utils.Constants;
import io.surisoft.cagent.utils.ServiceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AgentExecutor {
    private static final Logger logger = LoggerFactory.getLogger(AgentExecutor.class);
    private ApiClient apiClient;
    private AgentEnvironment agentEnvironment;
    private ConsulService consulService;
    private ServiceUtils serviceUtils;
    private final Map<String, Service> localServices = new HashMap<>();

    public AgentExecutor(ApiClient apiClient, AgentEnvironment agentEnvironment, ConsulService consulService, ServiceUtils serviceUtils) {
        this.apiClient = apiClient;
        this.agentEnvironment = agentEnvironment;
        this.consulService = consulService;
        this.serviceUtils = serviceUtils;
    }

    public void start() {
        ScheduledExecutorService deploymentExecutor = Executors.newSingleThreadScheduledExecutor();
        ScheduledExecutorService serviceExecutor = Executors.newSingleThreadScheduledExecutor();
        deploymentExecutor.scheduleAtFixedRate(
                checkForDeployments, agentEnvironment.getExecutorInitialDelay(),
                agentEnvironment.getExecutorExecutionInterval(),
                TimeUnit.SECONDS);
        serviceExecutor.scheduleAtFixedRate(
                checkForServices, agentEnvironment.getExecutorInitialDelay(),
                agentEnvironment.getExecutorExecutionInterval(),
                TimeUnit.SECONDS);
    }

    Runnable checkForDeployments = () -> {
        try {
            AppsV1Api appsV1Api = new AppsV1Api(apiClient);
            try (Watch<V1Deployment> watch = Watch.createWatch(
                    apiClient,
                    appsV1Api.listDeploymentForAllNamespacesCall(null, null, null, null, 100, "false",
                            null, null, null, 10, true, null),
                    new TypeToken<Watch.Response<V1Deployment>>(){}.getType())) {
                for (Watch.Response<V1Deployment> event : watch) {
                    V1ObjectMeta deploymentMetadata = event.object.getMetadata();
                    if(event.type.equals(Constants.KUBERNETES_ADDED_EVENT) && !localServices.containsKey(Objects.requireNonNull(event.object.getMetadata()).getName())) {
                        if(registerService(deploymentMetadata, agentEnvironment)) {
                            logger.info("New Deployment {} detected, adding to the list", event.object.getMetadata().getName());
                            Service service = serviceUtils.createService(event.object);
                            localServices.put(event.object.getMetadata().getName(), service);
                        }
                    } else if (event.type.equals(Constants.KUBERNETES_DELETED_EVENT)) {
                        if(registerService(deploymentMetadata, agentEnvironment)) {
                            logger.info("Deleted service {} detected, removing from list", Objects.requireNonNull(event.object.getMetadata()).getName());
                            Service service = localServices.get(event.object.getMetadata().getName());
                            consulService.deregisterService(service);
                            localServices.remove(event.object.getMetadata().getName());
                        }
                    }
                }
            } catch (ApiException e) {
                logger.error(e.getResponseBody());
                logger.error(e.getCode()+"");
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    };
    Runnable checkForServices = () -> {
        try {
            CoreV1Api coreV1Api = new CoreV1Api(apiClient);
            try (Watch<V1Service> watch = Watch.createWatch(
                    apiClient,
                        coreV1Api.listServiceForAllNamespacesCall(null, null, null, null, 100, "false",
                            null, null, null, 10, true, null),
                    new TypeToken<Watch.Response<V1Service>>(){}.getType())) {
                for (Watch.Response<V1Service> event : watch) {
                    if(event.type.equals(Constants.KUBERNETES_ADDED_EVENT) &&
                            localServices.containsKey(Objects.requireNonNull(event.object.getMetadata()).getName()) &&
                            !localServices.get(event.object.getMetadata().getName()).isRegistered()) {
                        localServices.get(event.object.getMetadata().getName()).setAddress(serviceUtils.findServiceAddress(event.object));
                        localServices.get(event.object.getMetadata().getName()).setPort(serviceUtils.findServicePort(event.object));
                        consulService.registerService(localServices.get(event.object.getMetadata().getName()));
                    }
                }
            } catch (ApiException e) {
                logger.error(e.getResponseBody());
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    };

    private boolean registerService(V1ObjectMeta objectMeta, AgentEnvironment agentEnvironment) {
        for(String label : agentEnvironment.getLabelsToFilter()) {
            if(Objects.requireNonNull(objectMeta.getLabels()).containsKey(label)) {
                return true;
            }
        }
        return false;
    }
}