package io.surisoft.cagent.utils;

import io.kubernetes.client.openapi.models.V1Ingress;
import io.surisoft.cagent.schema.CapiAnnotations;
import io.surisoft.cagent.schema.Check;
import io.surisoft.cagent.schema.Ingress;
import io.surisoft.cagent.schema.Meta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class CapiAgentUtils {

    private static final Logger logger = LoggerFactory.getLogger(CapiAgentUtils.class);

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

                if(v.containsKey(CapiAnnotations.CAPI_META_OPEN_API)) {
                    meta.setOpenApiEndpoint(v.get(CapiAnnotations.CAPI_META_OPEN_API));
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