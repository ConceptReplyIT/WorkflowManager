package it.reply.workflowmanager.orchestrator.bpm.WIHs;

import com.google.common.collect.Maps;

import it.reply.workflowmanager.orchestrator.bpm.WIHs.misc.SignalEvent;
import it.reply.workflowmanager.utils.Constants;

import org.drools.core.process.instance.WorkItemManager;
import org.drools.core.process.instance.impl.WorkItemImpl;
import org.kie.api.executor.CommandContext;
import org.kie.api.executor.ExecutionResults;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.WorkItem;
import org.kie.internal.runtime.manager.RuntimeManagerRegistry;
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.Map;
import java.util.Map.Entry;

public final class EJBWorkItemHelper {

  private static final Logger LOG = LoggerFactory.getLogger(EJBWorkItemHelper.class);
      
  private EJBWorkItemHelper() {
  }

  public static CommandContext buildCommandContext(WorkItem workItem, Logger externalLogger) {
    String businessKey = buildBusinessKey(workItem);

    Logger logger = externalLogger != null ? externalLogger : LOG;
    logger.debug("Executing work item {} with built business key {}", workItem, businessKey);

    CommandContext ctxCMD = new CommandContext();
    ctxCMD.setData(Constants.BUSINESS_KEY, businessKey);
    ctxCMD.setData(Constants.WORKITEM, workItem);
    ctxCMD.setData(Constants.PROCESS_INSTANCE_ID, getProcessInstanceId(workItem));
    ctxCMD.setData(Constants.DEPLOYMENT_ID, getDeploymentId(workItem));

    return ctxCMD;
  }

  public static String buildBusinessKey(WorkItem workItem) {
    StringBuilder businessKey = new StringBuilder();
    businessKey.append(getProcessInstanceId(workItem));
    businessKey.append(":");
    businessKey.append(workItem.getId());
    return businessKey.toString();
  }

  public static long getProcessInstanceId(WorkItem workItem) {
    return ((WorkItemImpl) workItem).getProcessInstanceId();
  }

  public static long getProcessInstanceId(CommandContext ctx) {
    return (Long) ctx.getData(Constants.PROCESS_INSTANCE_ID);
  }

  public static String getDeploymentId(WorkItem workItem) {
    return ((WorkItemImpl) workItem).getDeploymentId();
  }

  public static String getDeploymentId(CommandContext ctx) {
    return (String) ctx.getData(Constants.DEPLOYMENT_ID);
  }

  public static void signalEvent(KieSession ksession, String eventName, Object eventPayload,
      Long processId) {
    ksession.signalEvent(eventName, eventPayload, processId);
  }

  @Deprecated
  public static void signalEvent(WorkItemManager workItemManager, String eventName,
      Object eventPayload, Long processId) {
    ((org.drools.core.process.instance.WorkItemManager) workItemManager).signalEvent(eventName,
        eventPayload, processId);
  }

  public static RuntimeManager getRuntimeManager(CommandContext ctx) {
    String deploymentId = getDeploymentId(ctx);
    RuntimeManager runtimeManager = RuntimeManagerRegistry.get().getManager(deploymentId);

    if (runtimeManager == null) {
      throw new IllegalStateException("There is no runtime manager for deployment " + deploymentId);
    }

    return runtimeManager;
  }

  public static RuntimeEngine getRuntimeEngine(RuntimeManager manager, CommandContext ctx) {
    RuntimeEngine engine = manager
        .getRuntimeEngine(ProcessInstanceIdContext.get(getProcessInstanceId(ctx)));
    return engine;
  }
  
  public static WorkItem getWorkItem(CommandContext ctx) {
    return (WorkItem) ctx.getData(Constants.WORKITEM);
  }

  /**
   * If the workitem has failed (i.e. a <code>SignalEvent</code> is present in the results) an error
   * event will be signaled to the related process and the workitem will be aborted. Otherwise the
   * workitem will be completed against the {@link WorkItemManager}.
   * 
   * @param results
   *          the workitem's {@link ExecutionResults}
   * @param workItem
   *          the {@link WorkItem}
   * @param ctx
   *          the {@link CommandContext}
   * @param logger
   *          an optional logger to log events
   */
  @SuppressWarnings("unchecked")
  public static void checkWorkItemOutcome(ExecutionResults results, WorkItem workItem,
      CommandContext ctx, Logger externalLogger) {
    Logger logger = externalLogger != null ? externalLogger : LOG;
    logger.debug("About to complete workItem {}", workItem);
    SignalEvent<Error> signalEvent = (SignalEvent<Error>) results.getData(Constants.SIGNAL_EVENT);
    if (signalEvent != null) {
      Object payload = null;
      switch (signalEvent.getType()) {
        default:
        case ERROR:
          payload = results.getData(Constants.ERROR_RESULT);
          break;

      }
      RuntimeManager manager = getRuntimeManager(ctx);
      RuntimeEngine engine = null;
      try {
          engine = getRuntimeEngine(manager, ctx);
        signalEvent(engine.getKieSession(), signalEvent.getName(), payload,
            workItem.getProcessInstanceId());
        logger.debug(
              "Command threw {} signal with payload {}", signalEvent.getType(),
              payload);
      } finally {
        if (engine != null) {
          manager.disposeRuntimeEngine(engine);
        }
      }
    } else {
      logger.debug("Command executed successfully with results {}", results);
    }
  }

  public static void initMdcFromCtx(CommandContext ctx) {
    try {
      @SuppressWarnings("unchecked")
      Map<String, String> oldMdcCtx = MDC.getCopyOfContextMap();
      if (oldMdcCtx == null) {
        oldMdcCtx = Maps.newHashMap();
      }
      @SuppressWarnings("unchecked")
      Map<String, String> MDCctx = (Map<String, String>) ctx.getData("MDC");
      if (MDCctx != null) {
        for (Entry<String, String> entry : MDCctx.entrySet()) {
          String oldValue = oldMdcCtx.put(entry.getKey(), entry.getValue());
          if (oldValue != null) {
            LOG.warn("Overwriting MDC for key {}: MDC value {}, ctx value {}", entry.getKey(), oldValue, entry.getValue());
          }
        }
        MDC.setContextMap(oldMdcCtx);
      }
    } catch (Exception ex) {
      LOG.error("Error initializing MDC", ex);
    }
  }
  
  public static void putMdcInCtx(CommandContext ctx) {
    @SuppressWarnings("unchecked")
    Map<String, String> mdc = MDC.getCopyOfContextMap();
    if (mdc != null && !mdc.isEmpty()) {
      ctx.setData("MDC", mdc);
    }
  }

  public static void mdcCleanUp(CommandContext ctx) {
    try {
      @SuppressWarnings("unchecked")
      Map<String, String> MDCctx = (Map<String, String>) ctx.getData("MDC");
      if (MDCctx != null) {
        for (Entry<String, String> entry : MDCctx.entrySet()) {
          Object mdcValue = MDC.get(entry.getKey());
          if (mdcValue == null) {
            LOG.warn("Mismatch while cleaning MDC for key {}: no MDC value found, ctx value {}", entry.getKey(), entry.getValue());
          } else {
            if (!mdcValue.equals(entry.getValue())) {
              LOG.warn("Mismatch while cleaning MDC for key {}: MDC value {}, ctx value {}", entry.getKey(), mdcValue, entry.getValue());
            }
            MDC.remove(entry.getKey());
          }
        }
      }
    } catch (Exception ex) {
      LOG.error("Error cleaning MDC", ex);
    }
  }
}
