package io.surisoft.cagent;

import com.google.gson.reflect.TypeToken;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.apis.NetworkingV1Api;
import io.kubernetes.client.openapi.models.V1Ingress;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1Service;
import io.kubernetes.client.openapi.models.V1ServiceList;
import io.kubernetes.client.util.Watch;
import io.surisoft.cagent.schema.AgentEnvironment;
import io.surisoft.cagent.schema.CapiAnnotations;
import io.surisoft.cagent.schema.Ingress;
import io.surisoft.cagent.schema.Service;
import io.surisoft.cagent.service.ConsulService;
import io.surisoft.cagent.utils.Constants;
import io.surisoft.cagent.utils.CapiAgentUtils;
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
    private CapiAgentUtils capiAgentUtils;
    private final Map<String, Ingress> localIngresses = new HashMap<>();
    private final Map<String, Service> localServices = new HashMap<>();

    public AgentExecutor(ApiClient apiClient, AgentEnvironment agentEnvironment, ConsulService consulService, CapiAgentUtils capiAgentUtils) {
        this.apiClient = apiClient;
        this.agentEnvironment = agentEnvironment;
        this.consulService = consulService;
        this.capiAgentUtils = capiAgentUtils;
    }

    public void start() {
        ScheduledExecutorService ingressConsumer = Executors.newSingleThreadScheduledExecutor();
        ScheduledExecutorService ingressIntegrator = Executors.newSingleThreadScheduledExecutor();
        ingressConsumer.scheduleAtFixedRate(
                checkForIngresses, agentEnvironment.getExecutorInitialDelay(),
                agentEnvironment.getExecutorExecutionInterval(),
                TimeUnit.SECONDS);
        ingressIntegrator.scheduleAtFixedRate(
                registerIngresses, agentEnvironment.getExecutorInitialDelay(),
                agentEnvironment.getExecutorExecutionInterval(),
                TimeUnit.SECONDS);

        if(agentEnvironment.isListenForServices()) {
            ScheduledExecutorService serviceExecutor = Executors.newSingleThreadScheduledExecutor();
            serviceExecutor.scheduleAtFixedRate(
                    checkForServices, agentEnvironment.getExecutorInitialDelay(),
                    agentEnvironment.getExecutorExecutionInterval(),
                    TimeUnit.SECONDS);
        }
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
                    logger.debug(item.type);
                    logger.debug(item.object.toJson());
                    if (item.type.equals(Constants.KUBERNETES_ADDED_EVENT) && !localServices.containsKey(Objects.requireNonNull(item.object.getMetadata()).getName())) {
                        if (registerIngress(item.object.getMetadata())) {
                            logger.info("New Ingress {} detected, adding to the list", item.object.getMetadata().getName());
                            Ingress ingress = capiAgentUtils.createIngress(item.object);
                            localIngresses.put(ingress.getName(), ingress);
                        }
                    } else if (item.type.equals(Constants.KUBERNETES_DELETED_EVENT)) {
                        if (registerIngress(Objects.requireNonNull(item.object.getMetadata()))) {
                            logger.info("Deleted ingress {} detected, removing from list", Objects.requireNonNull(item.object.getMetadata()).getName());
                            Ingress ingress = localIngresses.get(item.object.getMetadata().getName());
                            consulService.deregisterIngress(ingress);
                            localIngresses.remove(item.object.getMetadata().getName());
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.debug("Error checking for ingresses.", e);
            logger.error(e.getMessage(), e);
        }
    };

    private final Runnable registerIngresses = () -> {
        localIngresses.forEach((k, v) -> {
            if (!v.isRegistered()) {
                logger.debug(v.getName());
                consulService.registerIngress(v);
            }
        });
    };

   private final Runnable checkForServices = () -> {

        try {
            proccessLocalServices();
          /*
            logger.debug("Checking for services....");
            CoreV1Api coreV1Api = new CoreV1Api(apiClient);

            Watch<V1Service> watch =
                    Watch.createWatch(
                            apiClient,
                            coreV1Api.listNamespacedService("test-agent")
                                    .watch(true)
                                    .buildCall(null),
                            new TypeToken<Watch.Response<V1Service>>() {}.getType());

            try {
                for (Watch.Response<V1Service> event : watch) {
                    if(event.type.equals(Constants.KUBERNETES_ADDED_EVENT) &&
                            localServices.containsKey(Objects.requireNonNull(event.object.getMetadata()).getName()) &&
                            !localServices.get(event.object.getMetadata().getName()).isRegistered()) {
                        localServices.get(event.object.getMetadata().getName()).setAddress(serviceUtils.findServiceAddress(event.object));
                        localServices.get(event.object.getMetadata().getName()).setPort(serviceUtils.findServicePort(event.object));
                        consulService.registerService(localServices.get(event.object.getMetadata().getName()));
                    }
                }
            } finally {
                watch.close();
            }*/
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    };

    /*private boolean registerService(V1ObjectMeta objectMeta, AgentEnvironment agentEnvironment) {
        for(String label : agentEnvironment.getLabelsToFilter()) {
            if(Objects.requireNonNull(objectMeta.getLabels()).containsKey(label)) {
                return true;
            }
        }
        return false;
    }*/

    private boolean registerIngress(V1ObjectMeta objectMeta) {
        return Objects.requireNonNull(objectMeta.getAnnotations()).containsKey(CapiAnnotations.CAPI_META_GROUP) ||
                objectMeta.getAnnotations().containsKey(CapiAnnotations.CAPI_META_INGRESS);
    }

    private void proccessLocalServices() {
        localServices.forEach((k, v) -> {
            if (!v.isRegistered()) {
                logger.debug(v.getName());
                if(v.getConsulType().equals("service")) {
                    try {
                        V1Service kubernetesService = getKubernetesService(v.getName());
                        if(kubernetesService != null) {
                            v.setAddress(capiAgentUtils.findServiceAddress(kubernetesService));
                            v.setPort(capiAgentUtils.findServicePort(kubernetesService));
                            consulService.registerService(v);
                        }
                    } catch(ApiException e) {
                        logger.error(e.getMessage(), e);
                    }
                } else {
                    logger.debug("INGRESS!!!!!!");

                }

            }
        });
    }

    private V1Service getKubernetesService(String serviceName) throws ApiException {
        CoreV1Api coreV1Api = new CoreV1Api(apiClient);
        V1ServiceList serviceList = coreV1Api.listNamespacedService("test-agent").execute();
        for(V1Service service : serviceList.getItems()) {
            if(service.getMetadata().getName().equals(serviceName)) {
                return service;
            }
        }
        return null;
    }




}