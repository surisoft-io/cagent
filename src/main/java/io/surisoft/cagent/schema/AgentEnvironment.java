package io.surisoft.cagent.schema;

import java.util.List;

public class AgentEnvironment {

    private String namespace;
    private String consulHost;
    private int consulPort;
    private String consulToken;
    private int executorInitialDelay;
    private int executorExecutionInterval;
    private boolean listenForServices;
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

    public int getConsulPort() {
        return consulPort;
    }

    public void setConsulPort(int consulPort) {
        this.consulPort = consulPort;
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

    public boolean isListenForServices() {
        return listenForServices;
    }

    public void setListenForServices(boolean listenForServices) {
        this.listenForServices = listenForServices;
    }

    public String getDefaultLogLevel() {
        return defaultLogLevel;
    }

    public void setDefaultLogLevel(String defaultLogLevel) {
        this.defaultLogLevel = defaultLogLevel;
    }
}