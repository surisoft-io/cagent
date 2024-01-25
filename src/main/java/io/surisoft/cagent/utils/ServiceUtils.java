package io.surisoft.cagent.utils;

import io.kubernetes.client.openapi.models.*;
import io.surisoft.cagent.schema.Meta;
import io.surisoft.cagent.schema.Service;

import java.util.List;
import java.util.Objects;

public class ServiceUtils {

    public String findServiceAddress(V1Service kubernetesService) {
        V1ServiceSpec serviceSpec = kubernetesService.getSpec();
        assert serviceSpec != null;
        if(Objects.equals(serviceSpec.getType(), "ClusterIP") ||
                Objects.equals(serviceSpec.getType(), "NodePort")  ||
                Objects.equals(serviceSpec.getType(), "LoadBalancer")) {
            return serviceSpec.getClusterIP();
        }
        return null;
    }

    public int findServicePort(V1Service kubernetesService) {
        V1ServiceSpec serviceSpec = kubernetesService.getSpec();
        assert serviceSpec != null;
        if(Objects.equals(serviceSpec.getType(), "ClusterIP") ||
                Objects.equals(serviceSpec.getType(), "NodePort")  ||
                Objects.equals(serviceSpec.getType(), "LoadBalancer")) {
            return Objects.requireNonNull(serviceSpec.getPorts()).getFirst().getPort();
        }
        return 0;
    }

    public Service createService(V1Deployment kubernetesService) {
        Service service = new Service();
        buildServiceMetadata(service, kubernetesService);
        service.setId(Objects.requireNonNull(kubernetesService.getMetadata()).getName() + "-" + service.getMeta().getGroup());
        service.setName(Objects.requireNonNull(kubernetesService.getMetadata()).getName());
        return service;
    }

    private void buildServiceMetadata(Service service, V1Deployment deployment) {
        Meta meta = new Meta();
        List<V1Container> containerList = Objects.requireNonNull(Objects.requireNonNull(deployment.getSpec()).getTemplate().getSpec()).getContainers();
        Objects.requireNonNull(containerList.getFirst().getEnv()).forEach(env -> {
            switch(env.getName()) {
                case "spring.cloud.consul.discovery.metadata.group": meta.setGroup(env.getValue()); break;
                case "spring.cloud.consul.discovery.metadata.ingress": meta.setIngress(env.getValue()); break;
                case "spring.cloud.consul.discovery.metadata.secured": meta.setSecured(env.getValue()); break;
                case "spring.cloud.consul.discovery.metadata.schema": meta.setSchema(env.getValue()); break;
                case "spring.cloud.consul.discovery.metadata.keep-group": meta.setKeepGroup(env.getValue()); break;
                case "spring.cloud.consul.discovery.metadata.open-api": meta.setOpenApiEndpoint(env.getValue()); break;
                case "spring.cloud.consul.discovery.metadata.opa-rego": meta.setOpaRego(env.getValue()); break;
                case "spring.cloud.consul.discovery.metadata.namespace": meta.setNamespace(env.getValue()); break;
                case "spring.cloud.consul.discovery.metadata.subscription-group": meta.setSubscriptionGroup(env.getValue()); break;
            }
        });
        service.setMeta(meta);
    }
}
