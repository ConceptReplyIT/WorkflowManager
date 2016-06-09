package it.reply.workflowmanager.orchestrator.bpm;

import it.reply.workflowmanager.exceptions.WorkflowException;

import org.kie.api.event.process.ProcessCompletedEvent;
import org.kie.api.event.process.ProcessEventListener;
import org.kie.api.event.process.ProcessNodeLeftEvent;
import org.kie.api.event.process.ProcessNodeTriggeredEvent;
import org.kie.api.event.process.ProcessStartedEvent;
import org.kie.api.event.process.ProcessVariableChangedEvent;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.internal.runtime.manager.SessionNotFoundException;
import org.kie.internal.runtime.manager.context.EmptyContext;
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import javax.annotation.PostConstruct;

/**
 * This class is the manager of Business Processes execution. It can be used to launch a new process
 * 
 * @author l.biava
 *
 */
public abstract class AbstractBusinessProcessManager implements BusinessProcessManager {

  public static Logger LOG = LoggerFactory.getLogger(BusinessProcessManager.class);

  private RuntimeManager singletonRuntimeManager;

  private RuntimeManager perProcessInstanceRuntimeManager;

  private RuntimeManager perRequestRuntimeManager;

  public void setSingletonRuntimeManager(RuntimeManager singletonRuntimeManager) {
    this.singletonRuntimeManager = singletonRuntimeManager;
  }

  public void setPerProcessInstanceRuntimeManager(RuntimeManager perProcessInstanceRuntimeManager) {
    this.perProcessInstanceRuntimeManager = perProcessInstanceRuntimeManager;
  }

  public void setPerRequestRuntimeManager(RuntimeManager perRequestRuntimeManager) {
    this.perRequestRuntimeManager = perRequestRuntimeManager;
  }

  @PostConstruct
  private void configure() {
    // use toString to make sure DI initializes the bean
    // this makes sure that RuntimeManager is started asap,
    // otherwise after server restart complete task won't move process
    // forward
    if (singletonRuntimeManager != null) {
      RuntimeEngine runtime =
          singletonRuntimeManager.getRuntimeEngine(ProcessInstanceIdContext.get());
      registerjBPMAuditing(runtime.getKieSession());
    }
    if (perProcessInstanceRuntimeManager != null) {
      perProcessInstanceRuntimeManager.toString();
    }
    if (perRequestRuntimeManager != null) {
      perRequestRuntimeManager.toString();
    }

  }

  @Override
  public ProcessInstance startProcess(String procName, Map<String, Object> params,
      RUNTIME_STRATEGY runtimeStrat) throws WorkflowException {
    try {
      RuntimeEngine runtime = null;
      RuntimeManager runtimeManager = null;
      KieSession ksession = null;
      switch (runtimeStrat) {
        case PER_PROCESS_INSTANCE:
          runtimeManager = perProcessInstanceRuntimeManager;
          runtime = runtimeManager.getRuntimeEngine(ProcessInstanceIdContext.get());
          ksession = runtime.getKieSession();
          registerjBPMAuditing(ksession);
          break;
        case PER_REQUEST:
          throw new UnsupportedOperationException(
              "Only singleton & per request RM supported currently.");
          // runtimeManager = perRequestRuntimeManager;
          // runtime = runtimeManager.getRuntimeEngine(EmptyContext.get());
          // ksession = runtime.getKieSession();
          // registerjBPMAuditing(ksession);
          // break;
        case SINGLETON:
          runtimeManager = singletonRuntimeManager;
          runtime = runtimeManager.getRuntimeEngine(EmptyContext.get());
          ksession = runtime.getKieSession();
          break;
        default:
          throw new IllegalArgumentException(
              "Unknown runtime strategy: " + runtimeStrat.toString());
      }

      ProcessInstance processInstance = null;
      processInstance = ksession.startProcess(procName, params);

      return processInstance;
    } catch (Throwable e) {
      throw new WorkflowException(e);
    }
  }

  @Override
  public void abortProcess(long processInstanceId, RUNTIME_STRATEGY runtimeStrat)
      throws WorkflowException {
    try {
      RuntimeEngine runtime = null;
      RuntimeManager runtimeManager = null;
      KieSession ksession = null;
      switch (runtimeStrat) {
        case PER_REQUEST:
        case PER_PROCESS_INSTANCE:
          runtimeManager = perProcessInstanceRuntimeManager;
          runtime =
              runtimeManager.getRuntimeEngine(ProcessInstanceIdContext.get(processInstanceId));
          try {
            ksession = runtime.getKieSession();
          } catch (SessionNotFoundException snfe) {
            LOG.warn("Error aborting process instance {}.", processInstanceId);
            return;
          }
          break;
        case SINGLETON:
          runtimeManager = singletonRuntimeManager;
          runtime = runtimeManager.getRuntimeEngine(EmptyContext.get());
          ksession = runtime.getKieSession();
          break;
        default:
          throw new IllegalArgumentException(
              "Unknown runtime strategy: " + runtimeStrat.toString());
      }
      try {
        ksession.abortProcessInstance(processInstanceId);
      } catch (IllegalArgumentException e) {
        LOG.warn("Error aborting process instance {}.", processInstanceId);
        return;
      }

    } catch (Throwable e) {
      throw new WorkflowException(e);
    }

  }

  private void registerjBPMAuditing(KieSession ksession) {
    // AbstractAuditLogger auditLogger = AuditLoggerFactory.newInstance(AuditLoggerFactory.Type.JPA,
    // ksession, null);

    // ksession.addEventListener(auditLogger);

    // Registering ProcessEventListener to track process instances status
    ksession.addEventListener(new ProcessEventListener() {

      @Override
      public void beforeProcessStarted(ProcessStartedEvent event) {
        LOG.info("PROCESS STARTED instanceId(" + event.getProcessInstance().getId() + ")");
      }

      @Override
      public void afterProcessCompleted(ProcessCompletedEvent event) {
        // Track processInstance completed
        LOG.info("PROCESS COMPLETED instanceId(" + event.getProcessInstance().getId() + ")");
      }

      @Override
      public void beforeVariableChanged(ProcessVariableChangedEvent event) {
      }

      @Override
      public void beforeProcessCompleted(ProcessCompletedEvent event) {
      }

      @Override
      public void beforeNodeTriggered(ProcessNodeTriggeredEvent event) {
      }

      @Override
      public void beforeNodeLeft(ProcessNodeLeftEvent event) {
      }

      @Override
      public void afterVariableChanged(ProcessVariableChangedEvent event) {
      }

      @Override
      public void afterProcessStarted(ProcessStartedEvent event) {
      }

      @Override
      public void afterNodeTriggered(ProcessNodeTriggeredEvent event) {
      }

      @Override
      public void afterNodeLeft(ProcessNodeLeftEvent event) {
      }

    });
  }

}
