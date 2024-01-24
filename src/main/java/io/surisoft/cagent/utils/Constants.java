package io.surisoft.cagent.utils;

import java.util.List;

public class Constants {
    public static final int EXECUTOR_INITIAL_DELAY_DEFAULT = 5;
    public static final int EXECUTOR_EXECUTION_INTERVAL_DEFAULT = 5;
    public static final String CONSUL_HOST_ENV_PROPERTY = "consulHost";
    public static final String CONSUL_TOKEN_ENV_PROPERTY = "consulToken";
    public static final String CONSUL_HOST_DEFAULT = "http://localhost:8500";
    public static final String EXECUTOR_INITIAL_DELAY_ENV_PROPERTY = "executorInitialDelay";
    public static final String EXECUTOR_EXECUTION_INTERVAL_ENV_PROPERTY = "executorInitialDelay";
    public static final String LABELS_TO_FILTER_ENV_PROPERTY = "labelsToFilter";
    public static final List<String> LABELS_TO_FILTER_DEFAULT = List.of( "capi");

}
