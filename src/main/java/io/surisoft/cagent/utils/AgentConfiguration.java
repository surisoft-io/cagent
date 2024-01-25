package io.surisoft.cagent.utils;

import io.surisoft.cagent.schema.AgentEnvironment;
import io.surisoft.cagent.service.ConsulService;
import okhttp3.OkHttpClient;

import java.util.List;

public class AgentConfiguration {
    public AgentEnvironment buildAgentEnvironment() {
        AgentEnvironment agentEnvironment = new AgentEnvironment();
        agentEnvironment.setConsulHost(
                System.getenv(Constants.CONSUL_HOST_ENV_PROPERTY) == null ?
                        Constants.CONSUL_HOST_DEFAULT :
                        System.getenv(Constants.CONSUL_HOST_ENV_PROPERTY));
        agentEnvironment.setConsulToken(
                System.getenv(Constants.CONSUL_TOKEN_ENV_PROPERTY) == null ?
                        null :
                        System.getenv(Constants.CONSUL_TOKEN_ENV_PROPERTY));
        agentEnvironment.setExecutorInitialDelay(
                System.getenv(Constants.EXECUTOR_INITIAL_DELAY_ENV_PROPERTY) == null ?
                        Constants.EXECUTOR_INITIAL_DELAY_DEFAULT :
                        Integer.parseInt(System.getenv(Constants.EXECUTOR_INITIAL_DELAY_ENV_PROPERTY)));
        agentEnvironment.setExecutorExecutionInterval(
                System.getenv(Constants.EXECUTOR_EXECUTION_INTERVAL_ENV_PROPERTY) == null ?
                        Constants.EXECUTOR_EXECUTION_INTERVAL_DEFAULT :
                        Integer.parseInt(System.getenv(Constants.EXECUTOR_EXECUTION_INTERVAL_ENV_PROPERTY)));
        agentEnvironment.setLabelsToFilter(
                System.getenv(Constants.LABELS_TO_FILTER_ENV_PROPERTY) == null ?
                        Constants.LABELS_TO_FILTER_DEFAULT :
                        List.of(System.getenv(Constants.LABELS_TO_FILTER_ENV_PROPERTY)));
        return agentEnvironment;
    }

    private OkHttpClient createHttpClient() {
        return new OkHttpClient.Builder().build();
    }

    public ConsulService createConsulService(AgentEnvironment agentEnvironment) {
        return new ConsulService(
                agentEnvironment.getConsulHost(), agentEnvironment.getConsulToken(), createHttpClient());
    }
}
