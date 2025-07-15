package io.surisoft.cagent.utils;

import io.kubernetes.client.openapi.models.*;
import io.surisoft.cagent.schema.CapiAnnotations;
import io.surisoft.cagent.schema.Ingress;
import io.surisoft.cagent.schema.Meta;
import io.surisoft.cagent.schema.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.*;

public class CapiAgentUtils {

    private static final Logger logger = LoggerFactory.getLogger(CapiAgentUtils.class);

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
        logger.debug("Ingress name {}", v1Ingress.getMetadata().getName());
        logger.debug("Ingress port {}", getIngressPort(v1Ingress.getMetadata()));
        Ingress ingress = new Ingress();
        ingress.setAddress(Objects.requireNonNull(Objects.requireNonNull(v1Ingress.getMetadata()).getAnnotations()).get(CapiAnnotations.CAPI_META_INGRESS));
        ingress.setPort(getIngressPort(v1Ingress.getMetadata()));
        ingress.setMetaList(buildMetadata(Objects.requireNonNull(Objects.requireNonNull(v1Ingress.getMetadata()).getAnnotations())));
        ingress.setName(v1Ingress.getMetadata().getName());
        ingress.setRegistered(false);
        return ingress;
    }

    private List<Meta> buildMetadata(Map<String, String> annotations) {
        List<Meta> metaList = new ArrayList<>();

        String group = annotations.get(CapiAnnotations.CAPI_META_GROUP);
        String ingressName = annotations.get(CapiAnnotations.CAPI_META_INGRESS);

        Map<String, Map<String, String>> parsedMeta = parseInstanceAnnotation(annotations);
        parsedMeta.forEach((k, v) -> {
            Meta meta = new Meta();
            meta.setCapiInstance(k);
            meta.setIngress(ingressName);
            meta.setGroup(group);

            if(v.containsKey(CapiAnnotations.CAPI_META_SECURED)) {
                meta.setSecured(v.get(CapiAnnotations.CAPI_META_SECURED));
            }
            if(v.containsKey(CapiAnnotations.CAPI_META_SCHEME)) {
                meta.setSchema(v.get(CapiAnnotations.CAPI_META_SCHEME));
            }
            if(v.containsKey(CapiAnnotations.CAPI_META_SUBSCRIPTION_GROUP)) {
                meta.setSubscriptionGroup(v.get(CapiAnnotations.CAPI_META_SUBSCRIPTION_GROUP));
            }
            if(v.containsKey(CapiAnnotations.CAPI_META_ROUTE_GROUP_FIRST)) {
                meta.setRouteGroupFirst(v.get(CapiAnnotations.CAPI_META_ROUTE_GROUP_FIRST));
            }
            metaList.add(meta);
        });
        return metaList;
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

    public Map<String, Map<String, String>> parseInstanceAnnotation(Map<String, String> annotations) {
        Map<String, Map<String, String>> result = new HashMap<>();
        for (Map.Entry<String, String> entry : annotations.entrySet()) {
            String key = entry.getKey();
            if (key.startsWith(Constants.CAPI_INSTANCE_ANNOTATION_PREFIX)) {
                String[] parts = key.substring(Constants.CAPI_INSTANCE_ANNOTATION_PREFIX.length() + 1).split("\\.", 2);
                if (parts.length == 2) {
                    String instance = parts[0];
                    String property = parts[1];
                    result.computeIfAbsent(instance, k -> new HashMap<>()).put(property, entry.getValue());
                }
            }
        }
        return result;
    }
}