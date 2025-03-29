package io.surisoft.cagent.utils;

import io.kubernetes.client.openapi.models.*;
import io.surisoft.cagent.schema.CapiAnnotations;
import io.surisoft.cagent.schema.Ingress;
import io.surisoft.cagent.schema.Meta;
import io.surisoft.cagent.schema.Service;

import java.net.URI;
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
        //buildMetadata(service, kubernetesService);
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
        ingress.setMeta(buildMetadata(Objects.requireNonNull(Objects.requireNonNull(v1Ingress.getMetadata()).getAnnotations())));
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

    private Meta buildMetadata(Map<String, String> annotations) {
        Meta meta = new Meta();
        meta.setGroup(annotations.get(CapiAnnotations.CAPI_META_GROUP));
        meta.setIngress(annotations.get(CapiAnnotations.CAPI_META_INGRESS));
        if(annotations.containsKey(CapiAnnotations.CAPI_META_SECURED)) {
            meta.setSecured(annotations.get(CapiAnnotations.CAPI_META_SECURED));
        }
        if(annotations.containsKey(CapiAnnotations.CAPI_META_SCHEME)) {
            meta.setSchema(annotations.get(CapiAnnotations.CAPI_META_SCHEME));
        }
        if(annotations.containsKey(CapiAnnotations.CAPI_META_INSTANCE)) {
            meta.setCapiInstance(annotations.get(CapiAnnotations.CAPI_META_INSTANCE));
        }
        if(annotations.containsKey(CapiAnnotations.CAPI_META_SUBSCRIPTION_GROUP)) {
            meta.setSubscriptionGroup(annotations.get(CapiAnnotations.CAPI_META_SUBSCRIPTION_GROUP));
        }
        if(annotations.containsKey(CapiAnnotations.CAPI_META_ROUTE_GROUP_FIRST)) {
            meta.setRouteGroupFirst(CapiAnnotations.CAPI_META_ROUTE_GROUP_FIRST);
        }
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