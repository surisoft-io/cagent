package io.surisoft.cagent;

import com.google.gson.reflect.TypeToken;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.apis.NetworkingV1Api;
import io.kubernetes.client.openapi.models.V1Ingress;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.util.Watch;
import io.surisoft.cagent.schema.AgentEnvironment;
import io.surisoft.cagent.schema.CapiAnnotations;
import io.surisoft.cagent.schema.Ingress;
import io.surisoft.cagent.service.ConsulService;
import io.surisoft.cagent.utils.CapiAgentUtils;
import io.surisoft.cagent.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AgentExecutor {
    private static final Logger logger = LoggerFactory.getLogger(AgentExecutor.class);
    private ApiClient apiClient;
    private AgentEnvironment agentEnvironment;
    private ConsulService consulService;
    private CapiAgentUtils capiAgentUtils;
    private final ScheduledExecutorService ingressScheduler;


    public AgentExecutor(ApiClient apiClient, AgentEnvironment agentEnvironment, ConsulService consulService, CapiAgentUtils capiAgentUtils) {
        this.apiClient = apiClient;
        this.agentEnvironment = agentEnvironment;
        this.consulService = consulService;
        this.capiAgentUtils = capiAgentUtils;
        this.ingressScheduler = createIngressScheduler();
    }

    public void start() {
        logger.debug("Starting ingress scheduler executor with time interval of {} seconds", agentEnvironment.getExecutorExecutionInterval());
        ingressScheduler.scheduleAtFixedRate(
                checkForIngresses, agentEnvironment.getExecutorInitialDelay(),
                agentEnvironment.getExecutorExecutionInterval(),
                TimeUnit.SECONDS);
    }

    private final Runnable checkForIngresses = () -> {
        try {
            logger.debug("Checking for ingresses....");
            NetworkingV1Api networkingV1Api = new NetworkingV1Api(apiClient);

            try (Watch<V1Ingress> watch = Watch.createWatch(
                    apiClient,
                    networkingV1Api.listNamespacedIngress(agentEnvironment.getNamespace())
                            .watch(true)
                            .buildCall(null),
                    new TypeToken<Watch.Response<V1Ingress>>() {
                    }.getType())) {
                for (Watch.Response<V1Ingress> item : watch) {
                    if (item.type.equals(Constants.KUBERNETES_ADDED_EVENT) && item.object.getMetadata() != null) {
                        if (registerIngress(item.object.getMetadata())) {
                            logger.info("New Ingress {} detected, adding to the list", item.object.getMetadata().getName());
                            List<Ingress> ingressList = capiAgentUtils.createIngress(item.object);
                            ingressList.forEach((ingress) -> {
                                consulService.registerIngress(ingress);
                            });
                        }
                    } else if (item.type.equals(Constants.KUBERNETES_DELETED_EVENT)) {
                        if (registerIngress(Objects.requireNonNull(item.object.getMetadata()))) {
                            logger.info("Deleted ingress {} detected, removing from list", Objects.requireNonNull(item.object.getMetadata()).getName());
                            List<Ingress> ingressList = capiAgentUtils.createIngress(item.object);
                            consulService.deregisterIngress(ingressList);
                        }
                    } else {
                        V1Ingress ingress = item.object;
                        String name = "undetermined";
                        if(ingress.getMetadata() != null && ingress.getMetadata().getName() != null) {
                            name = ingress.getMetadata().getName();
                        }
                        logger.debug("Ignoring ingress {} with event type {}", name, item.type);
                    }
                }
            }
        } catch (Exception e) {
            logger.debug("Error checking for ingresses.", e);
            logger.error(e.getMessage(), e);
        }
    };

    private boolean registerIngress(V1ObjectMeta objectMeta) {
        return objectMeta.getAnnotations() != null && objectMeta.getAnnotations().containsKey(CapiAnnotations.CAPI_META_AWARE);
    }

    private ScheduledExecutorService createIngressScheduler() {
        return Executors.newSingleThreadScheduledExecutor();
    }
}