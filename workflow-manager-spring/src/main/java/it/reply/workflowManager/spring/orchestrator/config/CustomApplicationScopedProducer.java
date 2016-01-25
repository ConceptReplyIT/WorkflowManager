package it.reply.workflowManager.spring.orchestrator.config;

import java.util.HashMap;
import javax.persistence.EntityManagerFactory;

import org.jbpm.executor.ExecutorServiceFactory;
import org.jbpm.runtime.manager.impl.SimpleRegisterableItemsFactory;
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
  public static PlatformTransactionManager platformTransactionManager() {
    return new JtaTransactionManager();
  }

  @Bean
  public static EntityManagerFactory entityManagerFactory() {
    LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();

    factory.setPersistenceUnitName(Constants.PERSISTENCE_UNIT_NAME);
    factory.setPersistenceXmlLocation("classpath:persistence.xml");
    return factory.getObject();
  }

  @Bean
  public static ExecutorService getyExecutorService() {
    ExecutorService executorService = ExecutorServiceFactory
        .newExecutorService(entityManagerFactory());
    return executorService;
  }

  @Bean
  public static SimpleRegisterableItemsFactory getRegisterableItemsFactory() {
    SimpleRegisterableItemsFactory srif = new SimpleRegisterableItemsFactory();
    return srif;
  }

  @Bean
  public static RuntimeEnvironment getRuntimeEnvironment() throws Exception {
    RuntimeEnvironmentFactoryBean refb = new RuntimeEnvironmentFactoryBean();
    refb.setType(RuntimeEnvironmentFactoryBean.TYPE_EMPTY);
    refb.setEntityManagerFactory(entityManagerFactory());

    // refb.setUserGroupCallback(userGroupCallback);
    refb.setRegisterableItemsFactory(getRegisterableItemsFactory());
    refb.setPessimisticLocking(true);
    refb.setTransactionManager(platformTransactionManager());
    refb.setEnvironmentEntries(new HashMap<String, Object>());
    refb.getEnvironmentEntries().put(Constants.EXECUTOR_SERVICE, getyExecutorService());
    return (RuntimeEnvironment) refb.getObject();
  }

  @Bean(destroyMethod = "close")
  @Qualifier("SINGLETON")
  public static RuntimeManager getSingletonRuntimeManager() throws Exception {
    RuntimeManagerFactoryBean rmfb = new RuntimeManagerFactoryBean();
    rmfb.setIdentifier("default-singleton");
    rmfb.setRuntimeEnvironment(getRuntimeEnvironment());
    rmfb.setType("SINGLETON");
    return (RuntimeManager) rmfb.getObject();
  }

  @Bean(destroyMethod = "close")
  @Qualifier("PER_PROCESS_INSTANCE")
  public static RuntimeManager getRuntimeManager() throws Exception {
    RuntimeManagerFactoryBean rmfb = new RuntimeManagerFactoryBean();
    rmfb.setIdentifier("default-per-process");
    rmfb.setRuntimeEnvironment(getRuntimeEnvironment());
    rmfb.setType("PER_PROCESS_INSTANCE");
    return (RuntimeManager) rmfb.getObject();
  }
}
