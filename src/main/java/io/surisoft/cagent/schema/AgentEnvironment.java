package io.surisoft.cagent.schema;

import java.util.List;

public class AgentEnvironment {
    private String namespace;
    private String consulHost;
    private int consulPort;
    private String consulToken;
    private int executorInitialDelay;
    private int executorExecutionInterval;
    private List<String> labelsToFilter;

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

    public List<String> getLabelsToFilter() {
        return labelsToFilter;
    }

    public void setLabelsToFilter(List<String> labelsToFilter) {
        this.labelsToFilter = labelsToFilter;
    }
}