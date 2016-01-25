package it.reply.workflowManager.orchestrator.bpm.WIHs;

import it.reply.workflowManager.orchestrator.bpm.commands.DispatcherCommand;

/* Copyright 2013 JBoss by Red Hat. */

import it.reply.workflowManager.utils.Constants;

import org.jbpm.executor.impl.wih.AsyncWorkItemHandler;
import org.jbpm.executor.impl.wih.AsyncWorkItemHandlerCmdCallback;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.kie.api.executor.CommandCallback;
import org.kie.api.executor.CommandContext;
import org.kie.api.executor.ExecutionResults;
import org.kie.api.executor.ExecutorService;
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * Asynchronous work item handler that utilizes power of <code>ExecutorService</code>. it expects
 * following parameters to be present on work item for proper execution:
 * <ul>
 * <li>CommandClass - FQCN of the command to be executed - mandatory unless this handler is
 * configured with default command class</li>
 * <li>Retries - number of retires for the command execution - optional</li>
 * </ul>
 * During execution it will set contextual data that will be available inside the command:
 * <ul>
 * <li>businessKey - generated from process instance id and work item id in following format:
 * [processInstanceId]:[workItemId]</li>
 * <li>workItem - actual work item instance that is being executed (including all parameters)</li>
 * <li>processInstanceId - id of the process instance that triggered this work item execution</li>
 * </ul>
 * 
 * In case work item shall be aborted handler will attempt to cancel active requests based on
 * business key (process instance id and work item id)
 */
public class AsyncEJBWorkItemHandler extends AsyncWorkItemHandler implements WorkItemHandler {

  private static final Logger logger = LogManager.getLogger(AsyncEJBWorkItemHandler.class);

  // private String commandClass;
  private ExecutorService executorService;

  private Class<? extends DispatcherCommand> distpacherCommandClass;

  public AsyncEJBWorkItemHandler(ExecutorService executorService,
      Class<? extends DispatcherCommand> distpacherCommandClass) {
    super(executorService, distpacherCommandClass.getCanonicalName());
    this.distpacherCommandClass = distpacherCommandClass;
    this.executorService = executorService;
  }

  @Override
  public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {

    String cmdClass = distpacherCommandClass.getCanonicalName();
    try {

      if (executorService == null || !executorService.isActive()) {
        throw new IllegalStateException("Executor is not set or is not active");
      }

      CommandContext ctxCMD = EJBWorkItemHelper.buildCommandContext(workItem, logger);

      ctxCMD.setData("callbacks", AsyncEJBWorkItemHandlerCmdCallback.class.getName());
      if (workItem.getParameter("Retries") != null) {
        ctxCMD.setData("retries", Integer.parseInt(workItem.getParameter("Retries").toString()));
      }

      logger.trace("Command context {}", ctxCMD);
      Long requestId = executorService.scheduleRequest(cmdClass, ctxCMD);
      logger.debug("Request scheduled successfully with id {}", requestId);
    } catch (Exception e) {
      logger.error("ERROR: Unable to instantiate requested command (" + cmdClass + ").", e);

      manager.abortWorkItem(-1);
    }

  }

  public static class AsyncEJBWorkItemHandlerCmdCallback extends AsyncWorkItemHandlerCmdCallback
      implements CommandCallback {

    @Override
    public void onCommandDone(CommandContext ctx, ExecutionResults results) {
      WorkItem workItem = (WorkItem) ctx.getData(Constants.WORKITEM);
      logger.debug("About to complete work item {}", workItem);

      // find the right runtime to do the complete
      RuntimeManager manager = getRuntimeManager(ctx);
      RuntimeEngine engine = manager.getRuntimeEngine(
          ProcessInstanceIdContext.get((Long) ctx.getData(Constants.PROCESS_INSTANCE_ID)));

      EJBWorkItemHelper.checkWorkItemOutcome(results, workItem, ctx, logger);

      manager.disposeRuntimeEngine(engine);
    }
  }
}
