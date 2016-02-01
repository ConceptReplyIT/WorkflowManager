package it.reply.workflowManager.cdi.orchestrator.bpm;

import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Startup;
import javax.inject.Inject;

import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.internal.runtime.manager.cdi.qualifier.PerProcessInstance;
import org.kie.internal.runtime.manager.cdi.qualifier.PerRequest;
import org.kie.internal.runtime.manager.cdi.qualifier.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.reply.workflowManager.orchestrator.bpm.AbstractBusinessProcessManager;

/**
 * This class is the manager of Business Processes execution. It can be used to launch a new process
 * 
 * @author l.biava
 *
 */
@Startup
@javax.ejb.Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class BusinessProcessManagerBean extends AbstractBusinessProcessManager {

  public static Logger LOG = LoggerFactory.getLogger(BusinessProcessManagerBean.class);

  @Inject
  @Singleton
  private RuntimeManager singletonRuntimeManager;

  @Inject
  @PerProcessInstance
  private RuntimeManager perProcessInstanceRuntimeManager;

  // @Inject
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
