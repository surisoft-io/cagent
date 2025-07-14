package io.surisoft.cagent;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.util.Config;
import io.surisoft.cagent.exception.AgentNamespaceException;
import io.surisoft.cagent.schema.AgentEnvironment;
import io.surisoft.cagent.service.ConsulService;
import io.surisoft.cagent.utils.AgentConfiguration;
import io.surisoft.cagent.utils.CapiAgentUtils;
import org.slf4j.*;

import java.io.IOException;


public class CapiKubernetesAgent {
    private static final Logger logger = LoggerFactory.getLogger(CapiKubernetesAgent.class);
    public static void configureAndStartAgent() throws AgentNamespaceException {
        try {
            logger.info("CAPI Agent: CAPI Kubernetes Agent is starting");

            logger.info("CAPI Agent: Creating Agent Configuration");
            AgentConfiguration agentConfiguration = new AgentConfiguration();
            AgentEnvironment agentEnvironment = agentConfiguration.buildAgentEnvironment();
            if(agentEnvironment.getNamespace() == null) {
                throw new AgentNamespaceException("Namespace is null");
            }
            logger.info("CAPI Agent: Setting Log level: {}", agentEnvironment.getDefaultLogLevel());

            CapiAgentUtils capiAgentUtils = new CapiAgentUtils();

            logger.info("CAPI Agent: Creating Consul Service");
            ConsulService consulService = agentConfiguration.createConsulService(agentEnvironment);

            logger.info("CAPI Agent: Creating Kubernetes Client Api");
            ApiClient apiClient = Config.defaultClient();

            logger.debug(apiClient.getJSON().toString());

            logger.info("CAPI Agent: Starting Agent Executor");
            AgentExecutor agentExecutor = new AgentExecutor(apiClient, agentEnvironment, consulService, capiAgentUtils);
            agentExecutor.start();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static void main(String[] args) throws AgentNamespaceException {
        configureAndStartAgent();
    }
}