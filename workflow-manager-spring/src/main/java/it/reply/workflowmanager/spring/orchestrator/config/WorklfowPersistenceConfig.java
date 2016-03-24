package it.reply.workflowmanager.spring.orchestrator.config;

import it.reply.workflowmanager.spring.orchestrator.annotations.WorkflowPersistenceUnit;

import org.jbpm.executor.ExecutorServiceFactory;
import org.kie.api.executor.ExecutorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.naming.NamingException;
import javax.persistence.EntityManagerFactory;

@Configuration
@EnableTransactionManagement(proxyTargetClass = true)
public class WorklfowPersistenceConfig {

  @Bean
  @Autowired
  public ExecutorService executorService(@WorkflowPersistenceUnit EntityManagerFactory emf)
      throws NamingException {
    ExecutorService executorService = ExecutorServiceFactory.newExecutorService(emf);
    return executorService;
  }
}
