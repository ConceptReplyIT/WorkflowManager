package it.reply.workflowmanager.spring.orchestrator.config;

import it.reply.workflowmanager.spring.orchestrator.jms.JmsSignalReceiverBean;

import org.jbpm.executor.impl.jms.JmsAvailableJobsExecutor;
import org.jbpm.process.audit.jms.AsyncAuditLogReceiver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.annotation.JmsListenerConfigurer;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerEndpointRegistrar;
import org.springframework.jms.config.SimpleJmsListenerEndpoint;
import org.springframework.jms.support.destination.DestinationResolver;
import org.springframework.jms.support.destination.JndiDestinationResolver;
import org.springframework.transaction.PlatformTransactionManager;

import javax.jms.ConnectionFactory;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

@Configuration
@EnableJms
public class JMSConfig implements JmsListenerConfigurer {

  @Autowired
  private JmsSignalReceiverBean JMSSignalReceiver;

  @Autowired
  private JmsAvailableJobsExecutor jmsAvailableJobsExecutor;

  @Autowired
  private AsyncAuditLogReceiver asyncAuditLogReceiver;

  @Autowired
  private PlatformTransactionManager transactionManager;

  @Override
  public void configureJmsListeners(JmsListenerEndpointRegistrar registrar) {
    try {
      registrar.setContainerFactory(jmsListenerContainerFactory());
    } catch (NamingException ex) {
      throw new RuntimeException("Failed to load JMS ContainerFactory", ex);
    }
    SimpleJmsListenerEndpoint endpoint = new SimpleJmsListenerEndpoint();
    endpoint.setId("audit");
    endpoint.setDestination("java:/queue/KIE.AUDIT.ALL");
    endpoint.setMessageListener(asyncAuditLogReceiver);
    endpoint.setConcurrency("1");
    registrar.registerEndpoint(endpoint);

    endpoint = new SimpleJmsListenerEndpoint();
    endpoint.setId("executor");
    endpoint.setDestination("java:/queue/KIE.EXECUTOR");
    endpoint.setMessageListener(jmsAvailableJobsExecutor);
    registrar.registerEndpoint(endpoint);

    endpoint = new SimpleJmsListenerEndpoint();
    endpoint.setId("signal");
    endpoint.setDestination("java:/queue/KIE.SIGNAL");
    endpoint.setMessageListener(JMSSignalReceiver);
    endpoint.setConcurrency("1");
    registrar.registerEndpoint(endpoint);
  }

  @Bean
  public ConnectionFactory connectionFactory() throws NamingException {
    Context ctx = new InitialContext();
    return (ConnectionFactory) ctx.lookup("java:/JmsXA");
  }

  @Bean
  public DestinationResolver destinationResolver() {
    DestinationResolver destinationResolver = new JndiDestinationResolver();
    return destinationResolver;
  }

  @Bean
  public DefaultJmsListenerContainerFactory jmsListenerContainerFactory() throws NamingException {
    DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
    factory.setTransactionManager(transactionManager);
    factory.setDestinationResolver(destinationResolver());
    factory.setConnectionFactory(connectionFactory());
    return factory;
  }
}