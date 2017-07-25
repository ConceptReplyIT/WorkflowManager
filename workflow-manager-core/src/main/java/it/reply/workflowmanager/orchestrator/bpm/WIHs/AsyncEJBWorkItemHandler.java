package it.reply.workflowmanager.orchestrator.bpm.WIHs;

import it.reply.workflowmanager.orchestrator.bpm.commands.DispatcherCommand;

/* Copyright 2013 JBoss by Red Hat. */

import org.jbpm.executor.impl.wih.AsyncWorkItemHandler;
import org.jbpm.executor.impl.wih.AsyncWorkItemHandlerCmdCallback;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.kie.api.executor.CommandContext;
import org.kie.api.executor.ExecutionResults;
import org.kie.api.executor.ExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

  private static final Logger logger = LoggerFactory.getLogger(AsyncEJBWorkItemHandler.class);

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

    String cmdClass = null;
    try {
      
      cmdClass = distpacherCommandClass.getCanonicalName();
      if (executorService == null || !executorService.isActive()) {
        throw new IllegalStateException("Executor is not set or is not active");
      }

      CommandContext ctxCMD = EJBWorkItemHelper.buildCommandContext(workItem, logger);

      ctxCMD.setData("callbacks", AsyncEJBWorkItemHandlerCmdCallback.class.getName());
      if (workItem.getParameter("Retries") != null) {
        ctxCMD.setData("retries", Integer.parseInt(workItem.getParameter("Retries").toString()));
      }
      EJBWorkItemHelper.putMdcInCtx(ctxCMD);
      
      logger.trace("Command context {}", ctxCMD);
      Long requestId = executorService.scheduleRequest(cmdClass, ctxCMD);
      logger.debug("Request scheduled successfully with id {}", requestId);
    } catch (Exception e) {
      logger.error("Unable to schedule command ({})", cmdClass, e);
      manager.abortWorkItem(workItem.getId());
    }
  }

  public static class AsyncEJBWorkItemHandlerCmdCallback extends AsyncWorkItemHandlerCmdCallback {

    @Override
    public void onCommandDone(CommandContext ctx, ExecutionResults results) {   
      try {
        EJBWorkItemHelper.initMdcFromCtx(ctx);
        EJBWorkItemHelper.checkWorkItemOutcome(results, ctx, logger);
        super.onCommandDone(ctx, results);
      } finally {
        EJBWorkItemHelper.mdcCleanUp(ctx);
      }
    }
  }
}
