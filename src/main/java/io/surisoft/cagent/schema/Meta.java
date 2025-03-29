package io.surisoft.cagent.schema;

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
    private String namespace;
    @JsonProperty("subscription-group")
    private String subscriptionGroup;

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

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getSubscriptionGroup() {
        return subscriptionGroup;
    }

    public void setSubscriptionGroup(String subscriptionGroup) {
        this.subscriptionGroup = subscriptionGroup;
    }
}
