package io.surisoft.cagent.schema;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Meta {
    private String group;
    @JsonProperty("keep-group")
    private String keepGroup;
    private String secured;
    private String ingress;
    @JsonProperty("opa-rego")
    private String opaRego;
    private String schema;
    @JsonProperty("open-api")
    private String openApiEndpoint;
    @JsonProperty("capi-instance")
    private String capiInstance;
    @JsonProperty("subscription-group")
    private String subscriptionGroup;
    @JsonProperty("route-group-first")
    private String routeGroupFirst;
    @JsonIgnore
    private String consulId;

    @JsonIgnore
    private String healthCheckPath;
    @JsonIgnore
    private String healthCheckInterval;
    @JsonIgnore
    private String getHealthCheckTimeout;

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getKeepGroup() {
        return keepGroup;
    }

    public void setKeepGroup(String keepGroup) {
        this.keepGroup = keepGroup;
    }

    public String getSecured() {
        return secured;
    }

    public void setSecured(String secured) {
        this.secured = secured;
    }

    public String getIngress() {
        return ingress;
    }

    public void setIngress(String ingress) {
        this.ingress = ingress;
    }

    public String getOpaRego() {
        return opaRego;
    }

    public void setOpaRego(String opaRego) {
        this.opaRego = opaRego;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getOpenApiEndpoint() {
        return openApiEndpoint;
    }

    public void setOpenApiEndpoint(String openApiEndpoint) {
        this.openApiEndpoint = openApiEndpoint;
    }

    public String getCapiInstance() {
        return capiInstance;
    }

    public void setCapiInstance(String capiInstance) {
        this.capiInstance = capiInstance;
    }

    public String getSubscriptionGroup() {
        return subscriptionGroup;
    }

    public void setSubscriptionGroup(String subscriptionGroup) {
        this.subscriptionGroup = subscriptionGroup;
    }

    public String getRouteGroupFirst() {
        return routeGroupFirst;
    }

    public void setRouteGroupFirst(String routeGroupFirst) {
        this.routeGroupFirst = routeGroupFirst;
    }

    public String getConsulId() {
        return consulId;
    }

    public void setConsulId(String consulId) {
        this.consulId = consulId;
    }

    public String getHealthCheckPath() {
        return healthCheckPath;
    }

    public void setHealthCheckPath(String healthCheckPath) {
        this.healthCheckPath = healthCheckPath;
    }

    public String getHealthCheckInterval() {
        return healthCheckInterval;
    }

    public void setHealthCheckInterval(String healthCheckInterval) {
        this.healthCheckInterval = healthCheckInterval;
    }

    public String getGetHealthCheckTimeout() {
        return getHealthCheckTimeout;
    }

    public void setGetHealthCheckTimeout(String getHealthCheckTimeout) {
        this.getHealthCheckTimeout = getHealthCheckTimeout;
    }
}
