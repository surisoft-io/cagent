package io.surisoft.cagent.utils;

public class Constants {
    public static final int EXECUTOR_INITIAL_DELAY_DEFAULT = 5;
    public static final int EXECUTOR_EXECUTION_INTERVAL_DEFAULT = 5;
    public static final String CONSUL_HOST_ENV_PROPERTY = "consulHost";
    public static final String CONSUL_TOKEN_ENV_PROPERTY = "consulToken";
    public static final String CONSUL_REGISTER_PATH = "/v1/agent/service/register?replace-existing-checks=true";
    public static final String CONSUL_DEREGISTER_PATH = "/v1/agent/service/deregister/";
    public static final String CONSUL_HOST_DEFAULT = "http://localhost:8500";
    public static final String EXECUTOR_INITIAL_DELAY_ENV_PROPERTY = "executorInitialDelay";
    public static final String EXECUTOR_EXECUTION_INTERVAL_ENV_PROPERTY = "executorInitialDelay";
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_TOKEN = "Bearer ";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String APPLICATION_JSON = "application/json";
    public static final String KUBERNETES_ADDED_EVENT = "ADDED";
    public static final String KUBERNETES_DELETED_EVENT = "DELETED";
    public static final String NAMESPACE = "namespace";
    public static final String DISCOVER_SERVICES = "discoverServices";
    public static final String LOG_LEVEL = "defaultLogLevel";
    public static final String HTTPS = "https";
    public static final String HTTP = "http";
    public static final String HTTPS_SCHEME = "https://";
    public static final String HTTP_SCHEME = "http://";
    public static final String CAPI_INSTANCE_ANNOTATION_PREFIX = "capi.meta.instance";
}