package it.reply.workflowManager.spring.orchestrator.config;

import java.util.HashMap;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.jbpm.executor.ExecutorServiceFactory;
import org.jbpm.runtime.manager.impl.SimpleRegisterableItemsFactory;
import org.jbpm.runtime.manager.impl.SimpleRuntimeEnvironment;
import org.kie.api.executor.ExecutorService;
import org.kie.api.runtime.manager.RuntimeEnvironment;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.spring.factorybeans.RuntimeEnvironmentFactoryBean;
import org.kie.spring.factorybeans.RuntimeManagerFactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.jta.JtaTransactionManager;
import it.reply.workflowManager.utils.Constants;

@Configuration
@EnableTransactionManagement
public class CustomApplicationScopedProducer {

  @Bean
  public PlatformTransactionManager workflowTransactionManager() {
    return new JtaTransactionManager();
  }

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

  @Bean
  public SimpleRegisterableItemsFactory registerableItemsFactory() {
    SimpleRegisterableItemsFactory srif = new SimpleRegisterableItemsFactory();
    return srif;
  }

  @Bean
  public RuntimeEnvironment runtimeEnvironment() throws Exception {
    RuntimeEnvironmentFactoryBean refb = new RuntimeEnvironmentFactoryBean();
    refb.setType(RuntimeEnvironmentFactoryBean.TYPE_DEFAULT);
    refb.setEntityManagerFactory(workflowEntityManagerFactory().getObject());

    // refb.setUserGroupCallback(userGroupCallback);
    refb.setRegisterableItemsFactory(registerableItemsFactory());

    refb.setPessimisticLocking(true);
    refb.setTransactionManager(workflowTransactionManager());
    refb.setEnvironmentEntries(new HashMap<String, Object>());
    refb.getEnvironmentEntries().put(Constants.EXECUTOR_SERVICE, executorService());
    SimpleRuntimeEnvironment env = (SimpleRuntimeEnvironment) refb.getObject();

    return env;
  }

  @Bean(destroyMethod = "close")
  @Qualifier("SINGLETON")
  public RuntimeManager singletonRuntimeManager() throws Exception {
    RuntimeManagerFactoryBean rmfb = new RuntimeManagerFactoryBean();
    rmfb.setIdentifier("default-singleton");
    rmfb.setRuntimeEnvironment(runtimeEnvironment());
    rmfb.setType("SINGLETON");
    return (RuntimeManager) rmfb.getObject();
  }

  @Bean(destroyMethod = "close")
  @Qualifier("PER_PROCESS_INSTANCE")
  public RuntimeManager perProcessInstanceRuntimeManager() throws Exception {
    RuntimeManagerFactoryBean rmfb = new RuntimeManagerFactoryBean();
    rmfb.setIdentifier("default-per-process");
    rmfb.setRuntimeEnvironment(runtimeEnvironment());
    rmfb.setType("PER_PROCESS_INSTANCE");
    return (RuntimeManager) rmfb.getObject();
  }

  @Bean(destroyMethod = "close")
  @Qualifier("PER_REQUEST")
  public RuntimeManager perRequesteRuntimeManager() throws Exception {
    RuntimeManagerFactoryBean rmfb = new RuntimeManagerFactoryBean();
    rmfb.setIdentifier("default-per-request");
    rmfb.setRuntimeEnvironment(runtimeEnvironment());
    rmfb.setType("PER_REQUEST");
    return (RuntimeManager) rmfb.getObject();
  }
}
