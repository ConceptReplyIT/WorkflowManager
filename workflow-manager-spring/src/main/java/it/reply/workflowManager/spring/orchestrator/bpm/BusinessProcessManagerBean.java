package it.reply.workflowManager.spring.orchestrator.bpm;

import it.reply.workflowManager.exceptions.WorkflowException;
import it.reply.workflowManager.orchestrator.bpm.AbstractBusinessProcessManager;
import it.reply.workflowManager.spring.orchestrator.annotations.PerProcessInstance;
import it.reply.workflowManager.spring.orchestrator.annotations.PerRequest;
import it.reply.workflowManager.spring.orchestrator.annotations.Singleton;

import java.util.Map;

import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.ProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
  @Override
  public void setSingletonRuntimeManager(@Singleton RuntimeManager singletonRuntimeManager) {
    super.setSingletonRuntimeManager(singletonRuntimeManager);
  }

  @Autowired
  @Override
  public void setPerProcessInstanceRuntimeManager(
      @PerProcessInstance RuntimeManager perProcessInstanceRuntimeManager) {
    super.setPerProcessInstanceRuntimeManager(perProcessInstanceRuntimeManager);
  }

  // @Autowired
  @Override
  public void setPerRequestRuntimeManager(@PerRequest RuntimeManager perRequestRuntimeManager) {
    super.setPerRequestRuntimeManager(perRequestRuntimeManager);
  }

  @Override
  @Transactional
  public ProcessInstance startProcess(String procName, Map<String, Object> params,
      RUNTIME_STRATEGY runtimeStrat) throws WorkflowException {
    return super.startProcess(procName, params, runtimeStrat);
  }

  @Override
  @Transactional
  public void abortProcess(long processInstanceId, RUNTIME_STRATEGY runtimeStrat)
      throws WorkflowException {
    super.abortProcess(processInstanceId, runtimeStrat);
  }

}
