package it.reply.workflowManager.spring.orchestrator.bpm;

import org.kie.api.runtime.manager.RuntimeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.reply.workflowManager.orchestrator.bpm.AbstractBusinessProcessManager;
import it.reply.workflowManager.spring.orchestrator.annotations.PerProcessInstance;
import it.reply.workflowManager.spring.orchestrator.annotations.PerRequest;
import it.reply.workflowManager.spring.orchestrator.annotations.Singleton;

/**
 * This class is the manager of Business Processes execution. It can be used to launch a new process
 * 
 * @author l.biava
 *
 */
@Service
public class BusinessProcessManagerBean extends AbstractBusinessProcessManager {

  public static Logger LOG = LoggerFactory.getLogger(BusinessProcessManagerBean.class);

  @Autowired
  @Singleton
  private RuntimeManager singletonRuntimeManager;

  @Autowired
  @PerProcessInstance
  private RuntimeManager perProcessInstanceRuntimeManager;

  // @Autowired
  @PerRequest
  private RuntimeManager perRequestRuntimeManager;

  @Override
  public RuntimeManager getSingletonRuntimeManager() {
    return singletonRuntimeManager;
  }

  @Override
  public RuntimeManager getPerProcessInstanceRuntimeManager() {
    return perProcessInstanceRuntimeManager;
  }

  @Override
  public RuntimeManager getPerRequestRuntimeManager() {
    return perRequestRuntimeManager;
  }

}
