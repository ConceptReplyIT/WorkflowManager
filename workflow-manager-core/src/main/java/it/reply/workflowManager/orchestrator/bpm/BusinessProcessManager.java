package it.reply.workflowManager.orchestrator.bpm;

import org.kie.api.runtime.process.ProcessInstance;

import it.reply.workflowManager.exceptions.WorkflowException;

import java.util.Map;

/**
 * This class is the manager of Business Processes execution. It can be used to launch a new process
 * 
 * @author l.biava
 *
 */
public interface BusinessProcessManager {

  public static enum RUNTIME_STRATEGY {
    //@formatter:off
    SINGLETON,
    PER_PROCESS_INSTANCE,
    PER_REQUEST
    //@formatter:on
  }

  /**
   * Starts the business process with given name using the given parameters.
   * 
   * @param procName
   *          The FQCN of the BP.
   * @param params
   *          The parameters to pass to the BP.
   * @param runtimeStrat
   *          The runtime strategy for the KieSession creation (Singleton, PerProcessInstance,
   *          PerRequest).
   * @return The ProcessInstance of the newly started process.
   * @throws Exception
   */
  public ProcessInstance startProcess(String procName, Map<String, Object> params,
      RUNTIME_STRATEGY runtimeStrat) throws WorkflowException;

  public void abortProcess(long processInstanceId, RUNTIME_STRATEGY runtimeStrat)
      throws WorkflowException;

}
