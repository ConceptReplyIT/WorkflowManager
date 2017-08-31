package it.reply.workflowmanager.spring.orchestrator.config;

import com.google.common.base.Preconditions;

import it.reply.workflowmanager.spring.orchestrator.jms.JmsSignalReceiverBean;

import org.jbpm.executor.impl.jms.JmsAvailableJobsExecutor;
import org.jbpm.process.audit.jms.AsyncAuditLogReceiver;
import org.springframework.jms.annotation.JmsListenerConfigurer;
import org.springframework.jms.config.JmsListenerEndpointRegistrar;
import org.springframework.jms.config.SimpleJmsListenerEndpoint;

/**
 * {@link JmsListenerConfigurer} for the Jbpm queues. Intialize it in a &#064;Configuration class
 * with this beans:
 * 
 * <pre>
 * &#064;Bean
 * public JmsListenerConfigurer jmsListenerConfigurer(JmsSignalReceiverBean jmsSignalReceiver,
 *     JmsAvailableJobsExecutor jmsAvailableJobsExecutor,
 *     AsyncAuditLogReceiver asyncAuditLogReceiver) {
 *   return new JmsListenerConfigurerImpl(jmsSignalReceiver, jmsAvailableJobsExecutor,
 *       asyncAuditLogReceiver);
 * }
 * 
 * &#064;Bean
 * public ConnectionFactory connectionFactory() throws NamingException {
 *   Context ctx = new InitialContext();
 *   return (ConnectionFactory) ctx.lookup("java:/JmsXA");
 * }
 *
 * &#064;Bean
 * public DestinationResolver destinationResolver() {
 *   DestinationResolver destinationResolver = new JndiDestinationResolver();
 *   return destinationResolver;
 * }
 *
 * &#064;Bean
 * public JmsListenerContainerFactory jmsListenerContainerFactory(
 *     PlatformTransactionManager transactionManager) throws NamingException {
 *   DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
 *   factory.setTransactionManager(transactionManager);
 *   factory.setDestinationResolver(destinationResolver());
 *   factory.setConnectionFactory(connectionFactory());
 *   return factory;
 * }
 * </pre>
 */
public class JmsListenerConfigurerImpl implements JmsListenerConfigurer {

  private final JmsSignalReceiverBean jmsSignalReceiver;

  private final JmsAvailableJobsExecutor jmsAvailableJobsExecutor;

  private final AsyncAuditLogReceiver asyncAuditLogReceiver;

  public JmsListenerConfigurerImpl(JmsSignalReceiverBean jmsSignalReceiver,
      JmsAvailableJobsExecutor jmsAvailableJobsExecutor,
      AsyncAuditLogReceiver asyncAuditLogReceiver) {
    this.jmsSignalReceiver = Preconditions.checkNotNull(jmsSignalReceiver);
    this.jmsAvailableJobsExecutor = Preconditions.checkNotNull(jmsAvailableJobsExecutor);
    this.asyncAuditLogReceiver = Preconditions.checkNotNull(asyncAuditLogReceiver);
  }
  
  public JmsListenerConfigurer jmsListenerConfigurer(JmsSignalReceiverBean jmsSignalReceiver,
      JmsAvailableJobsExecutor jmsAvailableJobsExecutor,
      AsyncAuditLogReceiver asyncAuditLogReceiver) {
    return new JmsListenerConfigurerImpl(jmsSignalReceiver, jmsAvailableJobsExecutor,
        asyncAuditLogReceiver);
  }
  
  @Override
  public void configureJmsListeners(JmsListenerEndpointRegistrar registrar) {
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
    endpoint.setMessageListener(jmsSignalReceiver);
    endpoint.setConcurrency("1");
    registrar.registerEndpoint(endpoint);
  }
}