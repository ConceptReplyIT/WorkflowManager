package it.reply.workflowmanager.orchestrator.bpm.WIHs;

import it.reply.workflowmanager.orchestrator.bpm.commands.DispatcherCommand;

import org.kie.api.executor.CommandContext;
import org.kie.api.executor.ExecutionResults;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
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
public class SyncEJBWorkItemHandler implements WorkItemHandler {

  private static final Logger logger = LoggerFactory.getLogger(SyncEJBWorkItemHandler.class);

  private Class<? extends DispatcherCommand> distpacherCommandClass;

  public SyncEJBWorkItemHandler(Class<? extends DispatcherCommand> distpacherCommandClass) {
    this.distpacherCommandClass = distpacherCommandClass;
  }

  @Override
  public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
    try {
      DispatcherCommand dispatcherCommand = distpacherCommandClass.newInstance();

      CommandContext ctxCMD = EJBWorkItemHelper.buildCommandContext(workItem, logger);

      logger.trace("Command context {}", ctxCMD);

      ExecutionResults exResults = dispatcherCommand.execute(ctxCMD);

      EJBWorkItemHelper.checkWorkItemOutcome(exResults, ctxCMD, logger);
      manager.completeWorkItem(workItem.getId(), exResults.getData());

    } catch (Exception e) {
      logger.error("Unable to instantiate requested command.", e);
      manager.abortWorkItem(workItem.getId());
      if (e instanceof InterruptedException) {
        Thread.currentThread().interrupt();
      }
    }

  }

  @Override
  public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
    // DO NOTHING
  }

}
