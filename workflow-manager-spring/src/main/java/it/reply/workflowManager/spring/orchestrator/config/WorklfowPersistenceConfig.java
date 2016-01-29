package it.reply.workflowManager.spring.orchestrator.config;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import org.jbpm.executor.ExecutorServiceFactory;
import org.kie.api.executor.ExecutorService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import it.reply.workflowManager.utils.Constants;

@Configuration
@EnableTransactionManagement(proxyTargetClass = true)
public class WorklfowPersistenceConfig {

  @Bean
  public static DataSource workflowDataSource() throws NamingException {
    Context ctx = new InitialContext();
    return (DataSource) ctx.lookup("java:jboss/datasources/WorkflowManager/JBPM-DS");
  }

  @Bean
  public LocalContainerEntityManagerFactoryBean workflowEntityManagerFactory()
      throws NamingException {
    LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
    factory.setPersistenceUnitName(Constants.PERSISTENCE_UNIT_NAME);
    factory.setPersistenceXmlLocation("classpath:/META-INF/persistence.xml");
    factory.setJtaDataSource(workflowDataSource());
    return factory;
  }

  @Bean
  public ExecutorService executorService() throws NamingException {
    ExecutorService executorService = ExecutorServiceFactory
        .newExecutorService(workflowEntityManagerFactory().getObject());
    return executorService;
  }
}
