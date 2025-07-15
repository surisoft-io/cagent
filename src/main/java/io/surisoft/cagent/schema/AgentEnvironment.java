package io.surisoft.cagent.schema;

public class AgentEnvironment {

    private String namespace;
    private String consulHost;
    private String consulToken;
    private int executorInitialDelay;
    private int executorExecutionInterval;
    private String defaultLogLevel;

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getConsulHost() {
        return consulHost;
    }

    public void setConsulHost(String consulHost) {
        this.consulHost = consulHost;
    }

    public String getConsulToken() {
        return consulToken;
    }

    public void setConsulToken(String consulToken) {
        this.consulToken = consulToken;
    }

    public int getExecutorInitialDelay() {
        return executorInitialDelay;
    }

    public void setExecutorInitialDelay(int executorInitialDelay) {
        this.executorInitialDelay = executorInitialDelay;
    }

    public int getExecutorExecutionInterval() {
        return executorExecutionInterval;
    }

    public void setExecutorExecutionInterval(int executorExecutionInterval) {
        this.executorExecutionInterval = executorExecutionInterval;
    }

    public String getDefaultLogLevel() {
        return defaultLogLevel;
    }

    public void setDefaultLogLevel(String defaultLogLevel) {
        this.defaultLogLevel = defaultLogLevel;
    }
}