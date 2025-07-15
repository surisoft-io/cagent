package io.surisoft.cagent.utils;

import io.kubernetes.client.openapi.models.*;
import io.surisoft.cagent.schema.*;
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

    public List<Ingress> createIngress(V1Ingress v1Ingress) {
        List<Ingress> ingressList = new ArrayList<>();
        if(v1Ingress.getMetadata() != null && v1Ingress.getMetadata().getAnnotations() != null) {
            buildMetadata(v1Ingress.getMetadata().getAnnotations(), v1Ingress.getMetadata().getName()).forEach(meta -> {
                Ingress ingress = new Ingress();
                ingress.setAddress(Objects.requireNonNull(Objects.requireNonNull(v1Ingress.getMetadata()).getAnnotations()).get(CapiAnnotations.CAPI_META_INGRESS));
                ingress.setName(v1Ingress.getMetadata().getName() + "-" + meta.getGroup() + "-" + meta.getCapiInstance());
                ingress.setId(v1Ingress.getMetadata().getName() + "-" + meta.getGroup() + "-" + meta.getCapiInstance());
                ingress.setMeta(meta);
                ingress.setRegistered(false);

                if(meta.getHealthCheckPath() != null) {
                    Check check = new Check();
                    check.setCheckId("Check-" + ingress.getName());
                    check.setHttp(meta.getHealthCheckPath());
                    check.setInterval(meta.getHealthCheckInterval());
                    check.setTimeout(meta.getGetHealthCheckTimeout());
                    ingress.setCheck(check);
                }
                ingressList.add(ingress);
            });

        }
        return ingressList;

        /*logger.debug("Ingress name {}", v1Ingress.getMetadata().getName());
        logger.debug("Ingress port {}", getIngressPort(v1Ingress.getMetadata()));
        Ingress ingress = new Ingress();
        ingress.setAddress(Objects.requireNonNull(Objects.requireNonNull(v1Ingress.getMetadata()).getAnnotations()).get(CapiAnnotations.CAPI_META_INGRESS));
        ingress.setPort(getIngressPort(v1Ingress.getMetadata()));
        ingress.setMetaList(buildMetadata(Objects.requireNonNull(Objects.requireNonNull(v1Ingress.getMetadata()).getAnnotations())));
        ingress.setName(v1Ingress.getMetadata().getName());
        ingress.setRegistered(false);
        return ingress;*/
    }

    private List<Meta> buildMetadata(Map<String, String> annotations, String ingressName) {
        List<Meta> metaList = new ArrayList<>();

        Map<String, Map<String, String>> parsedMeta = parseInstanceAnnotation(annotations);
        parsedMeta.forEach((k, v) -> {
            Meta meta = new Meta();
            meta.setCapiInstance(k);

            if(v.containsKey(CapiAnnotations.CAPI_META_INGRESS) && v.containsKey(CapiAnnotations.CAPI_META_GROUP)) {

                meta.setIngress(v.get(CapiAnnotations.CAPI_META_INGRESS));
                meta.setGroup(v.get(CapiAnnotations.CAPI_META_GROUP));

                if(v.containsKey("secured")) {
                    meta.setSecured(v.get("secured"));
                }

                if(v.containsKey("scheme")) {
                    meta.setSchema(v.get("scheme"));
                }

                if(v.containsKey("subscription-group")) {
                    meta.setSubscriptionGroup(v.get("subscription-group"));
                }

                if(v.containsKey("route-group-first")) {
                    meta.setRouteGroupFirst(v.get("route-group-first"));
                }

                if(v.containsKey(CapiAnnotations.CAPI_META_HEALTH_CHECK_PATH)) {
                    meta.setHealthCheckPath(buildHealthCheck(meta.getIngress(), v.get(CapiAnnotations.CAPI_META_HEALTH_CHECK_PATH), meta.getSchema()));
                    if(v.containsKey(CapiAnnotations.CAPI_META_HEALTH_CHECK_INTERVAL)) {
                        meta.setHealthCheckInterval(v.get(CapiAnnotations.CAPI_META_HEALTH_CHECK_INTERVAL));
                    }
                    if(v.containsKey(CapiAnnotations.CAPI_META_HEALTH_CHECK_TIMEOUT)) {
                        meta.setGetHealthCheckTimeout(v.get(CapiAnnotations.CAPI_META_HEALTH_CHECK_TIMEOUT));
                    }
                }
                metaList.add(meta);
            }


        });
        return metaList;
    }


    public static int getIngressPort(Meta meta) {
        String ingress;
        try {
            // Ensure the input has a scheme
            if (!meta.getIngress().startsWith(Constants.HTTP_SCHEME) && !meta.getIngress().startsWith(Constants.HTTPS_SCHEME)) {
                if(meta.getSchema() != null && meta.getSchema().equals(Constants.HTTP)) {
                    ingress = Constants.HTTP_SCHEME + meta.getIngress();
                } else if(meta.getSchema() != null && meta.getSchema().equals(Constants.HTTPS)) {
                    ingress = Constants.HTTPS_SCHEME + meta.getIngress();
                } else {
                    // Default to http for parsing
                    ingress = Constants.HTTP_SCHEME + meta.getIngress();
                }
            } else {
                ingress = meta.getIngress();
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
            throw new IllegalArgumentException("Invalid URL format: " + meta.getIngress(), e);
        }
    }

    public String buildHealthCheck(String ingress, String healthCheckPath, String scheme) {
        String healthCheck;

        // Ensure the input has a scheme
        if (!ingress.startsWith(Constants.HTTP_SCHEME) && !ingress.startsWith(Constants.HTTPS_SCHEME)) {
            if(scheme != null && scheme.equals(Constants.HTTP)) {
                healthCheck = Constants.HTTP_SCHEME + ingress + healthCheckPath;
            } else if(scheme != null && scheme.equals(Constants.HTTPS)) {
                healthCheck = Constants.HTTPS_SCHEME  + ingress + healthCheckPath;
            } else {
                // Default to http for parsing
                healthCheck = Constants.HTTP_SCHEME + ingress + healthCheckPath;
            }
        } else {
            healthCheck = ingress + healthCheckPath;
        }

        return healthCheck;

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