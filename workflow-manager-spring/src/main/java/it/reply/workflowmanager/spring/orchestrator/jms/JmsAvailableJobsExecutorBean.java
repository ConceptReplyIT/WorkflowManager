package it.reply.workflowmanager.spring.orchestrator.jms;

import javax.inject.Inject;

import org.jbpm.executor.impl.ClassCacheManager;
import org.jbpm.executor.impl.jms.JmsAvailableJobsExecutor;
import org.kie.api.executor.ExecutorQueryService;
import org.kie.api.executor.ExecutorStoreService;

public class JmsAvailableJobsExecutorBean extends JmsAvailableJobsExecutor {
  @Inject
  @Override
  public void setQueryService(ExecutorQueryService queryService) {
    super.setQueryService(queryService);
  }

  @Inject
  @Override
  public void setClassCacheManager(ClassCacheManager classCacheManager) {
    super.setClassCacheManager(classCacheManager);
  }

  @Inject
  @Override
  public void setExecutorStoreService(ExecutorStoreService executorStoreService) {
    super.setExecutorStoreService(executorStoreService);
  }
}