package it.reply.workflowManager.orchestrator.bpm.WIHs;

import it.reply.workflowManager.orchestrator.bpm.WIHs.misc.SignalEvent;
import it.reply.workflowManager.utils.Constants;

import org.drools.core.process.instance.WorkItemManager;
import org.drools.core.process.instance.impl.WorkItemImpl;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.executor.CommandContext;
import org.kie.api.executor.ExecutionResults;
import org.kie.internal.runtime.manager.RuntimeManagerRegistry;
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;
import org.apache.logging.log4j.Logger;

public final class EJBWorkItemHelper {
	
	private EJBWorkItemHelper() {
	}

	public static CommandContext buildCommandContext(WorkItem workItem,
			Logger logger) {
		String businessKey = buildBusinessKey(workItem);

		if (logger != null) {
			logger.debug("Executing work item {} with built business key {}",
					workItem, businessKey);
		}
		
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
	
	public static void signalEvent(KieSession ksession, String eventName,
			Object eventPayload, Long processId) {
		ksession.signalEvent(eventName, eventPayload, processId);
	}

	@Deprecated
	public static void signalEvent(WorkItemManager workItemManager, String eventName,
			Object eventPayload, Long processId) {
		((org.drools.core.process.instance.WorkItemManager) workItemManager).signalEvent(eventName, eventPayload, processId);
	}
	
	public static RuntimeManager getRuntimeManager(CommandContext ctx) {
		String deploymentId = getDeploymentId(ctx);
		RuntimeManager runtimeManager = RuntimeManagerRegistry.get()
				.getManager(deploymentId);

		if (runtimeManager == null) {
			throw new IllegalStateException(
					"There is no runtime manager for deployment "
							+ deploymentId);
		}

		return runtimeManager;
	}
	
	public static RuntimeEngine getRuntimeEngine(CommandContext ctx) {
		RuntimeManager manager = getRuntimeManager(ctx); 
		RuntimeEngine engine = manager
				.getRuntimeEngine(ProcessInstanceIdContext.get(getProcessInstanceId(ctx)));
		return engine;
	}
	
	/**
	 * If the workitem has failed (i.e. a <code>SignalEvent</code> is present in
	 * the results) an error event will be signaled to the related process and
	 * the workitem will be aborted. Otherwise the workitem will be completed
	 * against the {@link WorkItemManager}.
	 * 
	 * @param results
	 *            the workitem's {@link ExecutionResults}
	 * @param workItem
	 *            the {@link WorkItem}
	 * @param ctx
	 *            the {@link CommandContext}
	 * @param logger
	 *            an optional logger to log events
	 */
	@SuppressWarnings("unchecked")
	public static void checkWorkItemOutcome(ExecutionResults results,
			WorkItem workItem, CommandContext ctx, Logger logger) {
		RuntimeEngine engine = getRuntimeEngine(ctx);
		SignalEvent<Error> signalEvent = (SignalEvent<Error>) results.getData(Constants.SIGNAL_EVENT);
		if (signalEvent != null) {
			Object payload = null;
			switch (signalEvent.getType()) {
			default:
			case ERROR:
				payload = results.getData(Constants.ERROR_RESULT);
				break;
			
			}
			signalEvent(engine.getKieSession(), signalEvent.getName(),
					payload,
					workItem.getProcessInstanceId());


			if (logger != null)
				logger.debug("Command threw " + signalEvent.getType().toString().toLowerCase() + " signal {}",
						payload);

			// Abort the failed WorkItem
			try {
				engine.getKieSession().getWorkItemManager().abortWorkItem(-1);
			} catch (Exception e) {
				if (logger != null)
					logger.debug("Cannot abort workitem {}", workItem);
			}
		} else {
			if (logger != null)
				logger.debug("Command executed successfully with results {}",
						results);

			// Complete the successful WorkItem
			try {
				engine.getKieSession().getWorkItemManager()
						.completeWorkItem(workItem.getId(), results.getData());
			} catch (Exception e) {
				if (logger != null)
					logger.debug("Cannot complete workitem {}", workItem);
			}
		}
	}
}
