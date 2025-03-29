package io.surisoft.cagent.utils;

import io.kubernetes.client.openapi.models.*;
import io.surisoft.cagent.schema.CapiAnnotations;
import io.surisoft.cagent.schema.Ingress;
import io.surisoft.cagent.schema.Meta;
import io.surisoft.cagent.schema.Service;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CapiAgentUtils {

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
        if(kubernetesService.getMetadata().getLabels().containsKey("consul-type")) {
            service.setConsulType(kubernetesService.getMetadata().getLabels().get("consul-type"));
        }
        return service;
    }

    public Ingress createIngress(V1Ingress v1Ingress) {
        Ingress ingress = new Ingress();
        ingress.setAddress(Objects.requireNonNull(Objects.requireNonNull(v1Ingress.getMetadata()).getAnnotations()).get(CapiAnnotations.CAPI_META_INGRESS));
        ingress.setPort(getIngressPort(v1Ingress.getMetadata()));
        ingress.setMeta(buildIngressMetadata(Objects.requireNonNull(Objects.requireNonNull(v1Ingress.getMetadata()).getAnnotations())));
        ingress.setId(v1Ingress.getMetadata().getName() + "-" + ingress.getMeta().getGroup());
        ingress.setName(v1Ingress.getMetadata().getName());
        ingress.setRegistered(false);

        //buildServiceMetadata(service, ingress);
        //service.setId(Objects.requireNonNull(kubernetesService.getMetadata()).getName() + "-" + service.getMeta().getGroup());
        //service.setName(Objects.requireNonNull(kubernetesService.getMetadata()).getName());
        //if(kubernetesService.getMetadata().getLabels().containsKey("consul-type")) {
        //    service.setConsulType(kubernetesService.getMetadata().getLabels().get("consul-type"));
        //}
        return ingress;
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

    private Meta buildIngressMetadata(Map<String, String> annotations) {
        Meta meta = new Meta();
        meta.setGroup(annotations.get(CapiAnnotations.CAPI_META_GROUP));
        meta.setIngress(annotations.get(CapiAnnotations.CAPI_META_INGRESS));
        //and others
        return meta;
    }


    public static int getIngressPort(V1ObjectMeta ingressMetadata) {
        String ingress = Objects.requireNonNull(ingressMetadata.getAnnotations()).get(CapiAnnotations.CAPI_META_INGRESS);
        String metaDataScheme = Objects.requireNonNull(ingressMetadata.getAnnotations()).get(CapiAnnotations.CAPI_META_SCHEME);
        try {
            // Ensure the input has a scheme
            if (!ingress.startsWith(Constants.HTTP_SCHEME) && !ingress.startsWith(Constants.HTTPS_SCHEME)) {
                if(metaDataScheme != null && metaDataScheme.equals(Constants.HTTP)) {
                    ingress = Constants.HTTP_SCHEME + ingress;
                } else if(metaDataScheme != null && metaDataScheme.equals(Constants.HTTPS)) {
                    ingress = Constants.HTTPS_SCHEME + ingress;
                } else {
                    // Default to http for parsing
                    ingress = Constants.HTTP_SCHEME + ingress;
                }
            }

            URI uri = new URI(ingress);
            int port = uri.getPort();

            // If port is not explicitly given (-1 means no port in URI)
            if (port == -1) {
                String scheme = uri.getScheme();
                return scheme.equals("https") ? 443 : 80;
            }

            return port;
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid URL format: " + ingress, e);
        }
    }
}