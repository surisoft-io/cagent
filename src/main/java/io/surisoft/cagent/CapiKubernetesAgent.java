package io.surisoft.cagent;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.util.Config;
import io.surisoft.cagent.schema.AgentEnvironment;
import io.surisoft.cagent.service.ConsulService;
import io.surisoft.cagent.utils.AgentConfiguration;
import org.slf4j.*;

import java.io.IOException;


public class CapiKubernetesAgent {
    private static final Logger logger = LoggerFactory.getLogger(CapiKubernetesAgent.class);
    public static void configureAndStartAgent() {
        try {
            logger.info("CAPI Kubernetes Agent is starting...");

            logger.info("Creating Agent Configuration");
            AgentConfiguration agentConfiguration = new AgentConfiguration();
            AgentEnvironment agentEnvironment = agentConfiguration.buildAgentEnvironment();

            logger.info("Creating Consul Service");
            ConsulService consulService = agentConfiguration.createConsulService(agentEnvironment);

            logger.info("Creating Client Api");
            ApiClient apiClient = Config.defaultClient();

            logger.info("Starting Agent Executor...");
            AgentExecutor agentExecutor = new AgentExecutor();
            agentExecutor.start(apiClient, agentEnvironment, consulService);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }


    public static void main(String[] args) {
        configureAndStartAgent();
    }
}
