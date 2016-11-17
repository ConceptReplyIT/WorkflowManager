package it.reply.workflowmanager.spring.orchestrator.jms;

import org.jbpm.executor.impl.ClassCacheManager;
import org.jbpm.executor.impl.jms.JmsAvailableJobsExecutor;
import org.kie.api.executor.ExecutorQueryService;
import org.kie.api.executor.ExecutorStoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.Message;

@Component
@Transactional(propagation = Propagation.NOT_SUPPORTED)
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

  // Must run outside of a transaction context
  @Override
  @Transactional(propagation = Propagation.NOT_SUPPORTED)
  public void onMessage(Message message) {
    super.onMessage(message);
  }

}