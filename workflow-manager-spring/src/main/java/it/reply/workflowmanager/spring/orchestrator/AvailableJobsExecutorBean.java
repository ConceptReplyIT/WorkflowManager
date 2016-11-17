package it.reply.workflowmanager.spring.orchestrator;

import org.jbpm.executor.impl.AvailableJobsExecutor;
import org.jbpm.executor.impl.ClassCacheManager;
import org.kie.api.executor.ExecutorQueryService;
import org.kie.api.executor.ExecutorStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jndi.JndiTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.naming.NamingException;

@Component(AvailableJobsExecutorBean.BINDING_NAME)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class AvailableJobsExecutorBean extends AvailableJobsExecutor {

  public static Logger LOG = LoggerFactory.getLogger(AvailableJobsExecutorBean.class);

  private final JndiTemplate template = new JndiTemplate();

  public static final String BINDING_NAME = "AvailableJobsExecutorBean";

  @PostConstruct
  public void init() throws NamingException {
    template.bind(BINDING_NAME, this);
  }

  @PreDestroy
  public void destroy() {
    try {
      template.unbind(BINDING_NAME);
    } catch (NamingException ex) {
      LOG.warn("Failed to unbind AvailableJobsExecutorBean from jndi", ex);
    }
  }

  @Autowired
  @Override
  public void setQueryService(ExecutorQueryService queryService) {
    super.setQueryService(queryService);
  }

  @Autowired
  @Override
  public void setClassCacheManager(ClassCacheManager classCacheManager) {
    super.setClassCacheManager(classCacheManager);
  }

  @Autowired
  @Override
  public void setExecutorStoreService(ExecutorStoreService executorStoreService) {
    super.setExecutorStoreService(executorStoreService);
  }

  @Override
  @Async
  public void executeJob() {
    super.executeJob();
  }

}