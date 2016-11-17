package it.reply.workflowmanager.spring.orchestrator.config;

import it.reply.workflowmanager.orchestrator.config.ConfigProducer;
import it.reply.workflowmanager.spring.orchestrator.annotations.PerProcessInstance;
import it.reply.workflowmanager.spring.orchestrator.annotations.PerRequest;
import it.reply.workflowmanager.spring.orchestrator.annotations.Singleton;
import it.reply.workflowmanager.spring.orchestrator.annotations.WorkflowPersistenceUnit;
import it.reply.workflowmanager.utils.Constants;
import org.jbpm.runtime.manager.impl.SimpleRuntimeEnvironment;
import org.kie.api.executor.ExecutorService;
import org.kie.api.runtime.manager.RuntimeEnvironment;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.spring.factorybeans.RuntimeEnvironmentFactoryBean;
import org.kie.spring.factorybeans.RuntimeManagerFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.HashMap;

import javax.persistence.EntityManagerFactory;

@Configuration
public class WorkflowRuntimeConfig {

  @Autowired
  private ConfigProducer configProducer;

  @Autowired
  private AutowireableRegisterableItemsFactory customRegisterableItemsFactory;

  @Autowired
  @WorkflowPersistenceUnit
  private EntityManagerFactory entityManagerFactory;

  @Autowired
  private PlatformTransactionManager transactionManager;

  @Autowired
  private ExecutorService executorService;

  @Bean(destroyMethod = "close")
  public RuntimeEnvironment runtimeEnvironment() throws Exception {
    RuntimeEnvironmentFactoryBean refb = new RuntimeEnvironmentFactoryBean();
    refb.setType(RuntimeEnvironmentFactoryBean.TYPE_DEFAULT);
    refb.setEntityManagerFactory(entityManagerFactory);

    // refb.setUserGroupCallback(userGroupCallback);
    refb.setRegisterableItemsFactory(customRegisterableItemsFactory);

    refb.setPessimisticLocking(true);
    refb.setTransactionManager(transactionManager);
    refb.setEnvironmentEntries(new HashMap<String, Object>());
    refb.getEnvironmentEntries().put(Constants.EXECUTOR_SERVICE, executorService);
    SimpleRuntimeEnvironment env = (SimpleRuntimeEnvironment) refb.getObject();
    env.setUsePersistence(true);
    for (org.kie.api.io.Resource resource : configProducer.getJbpmResources()) {
      env.addAsset(resource, resource.getResourceType());
    }
    return env;
  }

  @Bean(destroyMethod = "close")
  @Singleton
  public RuntimeManager singletonRuntimeManager() throws Exception {
    RuntimeManagerFactoryBean rmfb = new RuntimeManagerFactoryBean();
    rmfb.setIdentifier("default-singleton");
    rmfb.setRuntimeEnvironment(runtimeEnvironment());
    rmfb.setType("SINGLETON");
    RuntimeManager rm = (RuntimeManager) rmfb.getObject();
    return rm;
  }

  @Bean(destroyMethod = "close")
  @PerProcessInstance
  public RuntimeManager perProcessInstanceRuntimeManager() throws Exception {
    RuntimeManagerFactoryBean rmfb = new RuntimeManagerFactoryBean();
    rmfb.setIdentifier("default-per-process");
    rmfb.setRuntimeEnvironment(runtimeEnvironment());
    rmfb.setType("PER_PROCESS_INSTANCE");
    RuntimeManager rm = (RuntimeManager) rmfb.getObject();
    return rm;
  }

  @Bean(destroyMethod = "close")
  @PerRequest
  public RuntimeManager perRequesteRuntimeManager() throws Exception {
    RuntimeManagerFactoryBean rmfb = new RuntimeManagerFactoryBean();
    rmfb.setIdentifier("default-per-request");
    rmfb.setRuntimeEnvironment(runtimeEnvironment());
    rmfb.setType("PER_REQUEST");
    RuntimeManager rm = (RuntimeManager) rmfb.getObject();
    return rm;
  }
}
