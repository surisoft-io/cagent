package io.surisoft.cagent.utils;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import io.surisoft.cagent.schema.AgentEnvironment;
import io.surisoft.cagent.service.ConsulService;
import okhttp3.OkHttpClient;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;

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
        agentEnvironment.setNamespace(
                System.getenv(Constants.NAMESPACE) == null ?
                        null :
                        System.getenv(Constants.NAMESPACE));
        agentEnvironment.setDefaultLogLevel(
                System.getenv(Constants.LOG_LEVEL) == null ?
                        "INFO":
                        System.getenv(Constants.LOG_LEVEL));

        Logger root = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.valueOf(agentEnvironment.getDefaultLogLevel()));
        return agentEnvironment;
    }

    private OkHttpClient createHttpClient() {
        try {
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(X509Certificate[] chain, String authType) {}
                        @Override
                        public void checkServerTrusted(X509Certificate[] chain, String authType) {}
                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[]{};
                        }
                    }
            };

            final SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
            builder.hostnameVerifier((hostname, session) -> true);

            return builder.build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public ConsulService createConsulService(AgentEnvironment agentEnvironment) {
        return new ConsulService(
                agentEnvironment.getConsulHost(), agentEnvironment.getConsulToken(), createHttpClient());
    }
}