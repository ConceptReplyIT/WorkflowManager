package it.reply.workflowmanager.spring.orchestrator.jms;

import org.jbpm.executor.impl.ClassCacheManager;
import org.jbpm.executor.impl.jms.JmsAvailableJobsExecutor;
import org.kie.api.executor.ExecutorQueryService;
import org.kie.api.executor.ExecutorStoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JmsAvailableJobsExecutorBean extends JmsAvailableJobsExecutor {

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
}