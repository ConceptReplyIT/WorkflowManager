package it.reply.workflowManager.orchestrator.bpm.WIHs;

/* Copyright 2013 JBoss by Red Hat. */

import it.reply.workflowManager.orchestrator.bpm.commands.EJBDispatcherCommand;
import it.reply.workflowManager.utils.Constants;

import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.kie.api.executor.CommandContext;
import org.kie.api.executor.ExecutionResults;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * Asynchronous work item handler that utilizes power of
 * <code>ExecutorService</code>. it expects following parameters to be present
 * on work item for proper execution:
 * <ul>
 * <li>CommandClass - FQCN of the command to be executed - mandatory unless this
 * handler is configured with default command class</li>
 * <li>Retries - number of retires for the command execution - optional</li>
 * </ul>
 * During execution it will set contextual data that will be available inside
 * the command:
 * <ul>
 * <li>businessKey - generated from process instance id and work item id in
 * following format: [processInstanceId]:[workItemId]</li>
 * <li>workItem - actual work item instance that is being executed (including
 * all parameters)</li>
 * <li>processInstanceId - id of the process instance that triggered this work
 * item execution</li>
 * </ul>
 * 
 * In case work item shall be aborted handler will attempt to cancel active
 * requests based on business key (process instance id and work item id)
 */
public class SyncEJBWorkItemHandler implements WorkItemHandler {

	private static final Logger logger = LogManager
			.getLogger(SyncEJBWorkItemHandler.class);

	public SyncEJBWorkItemHandler() {
	}

	@Override
	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
		try {
			ExecutionResults exResults = new ExecutionResults();
			EJBDispatcherCommand ejbDispatcherCommand = new EJBDispatcherCommand();
			
			String cmdClass;
			if ((String) workItem.getParameter(Constants.EJB_COMMAND_CLASS) != null) {
				cmdClass = (String) workItem.getParameter(Constants.EJB_COMMAND_CLASS);
				logger.debug("Command class for this execution is {}", cmdClass);
			} else {
				cmdClass = EJBDispatcherCommand.class.getName();
				logger.warn("Command class parameter was empty, using {} as fallback", cmdClass);
			}
			
			logger.debug("Command class for this execution is {}", cmdClass);

			CommandContext ctxCMD = EJBWorkItemHelper.buildCommandContext(
					workItem, logger);

			logger.trace("Command context {}", ctxCMD);

			exResults = ejbDispatcherCommand.execute(ctxCMD);

			EJBWorkItemHelper.checkWorkItemOutcome(exResults, workItem, ctxCMD,
					logger);

		} catch (Exception e) {
			logger.error("ERROR: Unable to instantiate requested command.", e);
			manager.abortWorkItem(-1);
		}

	}

	@Override
	public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
		// String businessKey = buildBusinessKey(workItem);
		// logger.info(
		// "Looking up for not cancelled and not done requests for business key {}",
		// businessKey);
		// List<RequestInfo> requests = executorService
		// .getRequestsByBusinessKey(businessKey);
		// if (requests != null) {
		// for (RequestInfo request : requests) {
		// if (request.getStatus() != STATUS.CANCELLED
		// && request.getStatus() != STATUS.DONE
		// && request.getStatus() != STATUS.ERROR) {
		// logger.info(
		// "About to cancel request with id {} and business key {} request state {}",
		// request.getId(), businessKey, request.getStatus());
		// executorService.cancelRequest(request.getId());
		// }
		// }
		// }
	}

}
