package it.reply.workflowManager.orchestrator.bpm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.DependsOn;
import javax.ejb.Local;
import javax.ejb.Startup;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javatuples.Pair;
import org.jbpm.process.audit.AbstractAuditLogger;
import org.jbpm.process.audit.AuditLoggerFactory;
import org.jbpm.workflow.instance.node.WorkItemNodeInstance;
import org.kie.api.event.process.ProcessCompletedEvent;
import org.kie.api.event.process.ProcessEventListener;
import org.kie.api.event.process.ProcessNodeLeftEvent;
import org.kie.api.event.process.ProcessNodeTriggeredEvent;
import org.kie.api.event.process.ProcessStartedEvent;
import org.kie.api.event.process.ProcessVariableChangedEvent;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.NodeInstance;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.process.WorkflowProcessInstance;
import org.kie.internal.runtime.manager.cdi.qualifier.PerProcessInstance;
import org.kie.internal.runtime.manager.cdi.qualifier.PerRequest;
import org.kie.internal.runtime.manager.cdi.qualifier.Singleton;
import org.kie.internal.runtime.manager.context.EmptyContext;
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;

@DependsOn("OrchestratorContext")
@Startup
@javax.inject.Singleton
//@ApplicationScoped
/**
 * This class is the manager of Business Processes execution. It can be used to launch a new process
 * 
 * @author l.biava
 *
 */
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
@Local(BusinessProcessManager.class)
public class BusinessProcessManager {

	public enum RUNTIME_STRATEGY {
		SINGLETON, PER_PROCESS_INSTANCE, PER_REQUEST
	}

	public static Logger LOG = LogManager
			.getLogger(BusinessProcessManager.class);


	@Inject
	@Singleton
	private RuntimeManager singletonRuntimeManager;

//	private RuntimeManager runtimeManager;

//	private KieSession ksession;

	 @Inject
	 @PerProcessInstance
	 private RuntimeManager perProcessInstanceRuntimeManager;

//	 @Inject
	 @PerRequest
	 private RuntimeManager perRequestRuntimeManager;

	@PostConstruct
	public void configure() {
		// use toString to make sure CDI initializes the bean
		// this makes sure that RuntimeManager is started asap,
		// otherwise after server restart complete task won't move process
		// forward
		if (singletonRuntimeManager != null) {
			singletonRuntimeManager.toString();
		}
		if (perProcessInstanceRuntimeManager != null) {
			perProcessInstanceRuntimeManager.toString();
		}
		if (perRequestRuntimeManager != null) {
			perRequestRuntimeManager.toString();
		}
		 

//		runtimeManager = perProcessInstanceRuntimeManager;

		System.out.println("Orchestrator started !");

		coldStart();
	}

	public RuntimeManager getSingletonRuntimeManager() {
		return singletonRuntimeManager;
	}

	 public RuntimeManager getPerProcessInstanceRuntimeManager() {
	 return perProcessInstanceRuntimeManager;
	 }
	
	 public RuntimeManager getPerRequestRuntimeManager() {
	 return perRequestRuntimeManager;
	 }

	/**
	 * Starts the business process with given name using the given parameters.
	 * 
	 * @param procName
	 *            The FQCN of the BP.
	 * @param params
	 *            The parameters to pass to the BP.
	 * @param runtimeStrat
	 *            The runtime strategy for the KieSession creation (Singleton,
	 *            PerProcessInstance, PerRequest).
	 * @return The ProcessInstance of the newly started process.
	 * @throws Exception
	 */
	public Pair<ProcessInstance, KieSession> startProcess(
			String procName, Map<String, Object> params,
			RUNTIME_STRATEGY runtimeStrat) throws Exception {

		RuntimeEngine runtime;
		RuntimeManager runtimeManager;

		// runtimeStrat=RUNTIME_STRATEGY.PER_REQUEST;
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
					"Only singleton Rm supported currently.");
//			 runtimeManager = perRequestRuntimeManager;
//			 runtime = runtimeManager.getRuntimeEngine(EmptyContext.get());
//			 ksession = runtime.getKieSession();
//			 registerjBPMAuditing(ksession);
//			 break;
		case SINGLETON:
			runtimeManager = singletonRuntimeManager;
			runtime = runtimeManager.getRuntimeEngine(EmptyContext.get());
			ksession = runtime.getKieSession();
			break;
		default:
			throw new IllegalArgumentException("Unknown runtime strategy: " + runtimeStrat.toString());
		}

		System.out.println("RUNTIME MANAGER:" + runtimeManager.getClass());

		Map<String, Object> completeWIResults = new HashMap<String, Object>();
		completeWIResults.put("RedoTask", true);

		// ksession.getWorkItemManager().registerWorkItemHandler(workItemName,
		// handler);
		/*
		 * AsyncWorkItemHandler asyncwih = new AsyncWorkItemHandler(
		 * executorService, "org.jbpm.examples.commands.ConsumeRestWSCommand");
		 * ksession.getWorkItemManager().registerWorkItemHandler("restWS",
		 * asyncwih);
		 */

		// TODO: use transaction !
		ProcessInstance processInstance;

		processInstance = ksession.createProcessInstance(procName, params);

//		WorkflowProcessInstance wfPI = (WorkflowProcessInstance) processInstance;

		// Start process
		processInstance = ksession
				.startProcessInstance(processInstance.getId());

		return new Pair<ProcessInstance, KieSession>(processInstance,
				ksession);
	}

	public static final boolean disableUpdate = true;
	
	private void registerjBPMAuditing(KieSession ksession) {
		// Registering logging for jBPM Process+WI (for debugging purposes)
		AbstractAuditLogger auditLogger = AuditLoggerFactory.newInstance(
				AuditLoggerFactory.Type.JPA, ksession, null);
		// .newJPAInstance(environment);

		ksession.addEventListener(auditLogger);

		// Registering ProcessEventListener to track process instances status
		ksession.addEventListener(new ProcessEventListener() {

			@Override
			public void beforeVariableChanged(ProcessVariableChangedEvent event) {
			}

			@Override
			public void beforeProcessStarted(ProcessStartedEvent event) {
				ProcessInstance procInst = event.getProcessInstance();

				System.out.println("Process started: name="
						+ procInst.getProcessName() + ", InstanceId="
						+ procInst.getId());

				LOG.info("PROCESS STARTED instanceId("
						+ event.getProcessInstance().getId() + ")");
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
			public void afterProcessCompleted(ProcessCompletedEvent event) {
				// Track processInstance completed
				LOG.info("PROCESS COMPLETED instanceId("
						+ event.getProcessInstance().getId() + ")");

				//ProcessInstance procInst = event.getProcessInstance();
			}

			@Override
			public void afterNodeTriggered(ProcessNodeTriggeredEvent event) {
				if (disableUpdate)
					return;
//				ProcessInstance procInst = event.getProcessInstance();
			}

			@Override
			public void afterNodeLeft(ProcessNodeLeftEvent event) {
				if (disableUpdate)
					return;
//				ProcessInstance procInst = event.getProcessInstance();

			}

		});

		List<Long> processesToResume = new ArrayList<Long>();
		for (Long procInstId : processesToResume) {
			try {
				resumePendingProcess(ksession, procInstId);
			} catch (Exception e) {
			}
		}
	}

	/**
	 * Tries to resume pending process instances after server cold start.
	 */
	private void coldStart() {

		if (singletonRuntimeManager != null) {
			RuntimeEngine runtime = singletonRuntimeManager.getRuntimeEngine(EmptyContext
					.get());
			registerjBPMAuditing(runtime.getKieSession());
		}
	}

	public void resumePendingProcess(KieSession ksession, long processInstanceId)
			throws Exception {

		// Forcing to complete the work item so the processInstance will go to
		// the next node
		try {
			ProcessInstance processInstance = ksession
					.getProcessInstance(processInstanceId);

			if (processInstance == null) {
				LOG.warn("PROCESS RESUME - Unable to resume process ("
						+ processInstance + "): no such processInstanceId.");
				throw new Exception(
						"PROCESS RESUME - Unable to resume process ("
								+ processInstance
								+ "): no such processInstanceId.");
			}

			WorkflowProcessInstance workflowProcessInstance = ((WorkflowProcessInstance) processInstance);
			NodeInstance nodeInstance = workflowProcessInstance
					.getNodeInstances().iterator().next();
			WorkItemNodeInstance workItemNodeInstance = null;
			if (nodeInstance instanceof WorkItemNodeInstance) {
				workItemNodeInstance = (WorkItemNodeInstance) nodeInstance;
			} else {
				LOG
						.warn("PROCESS RESUME - Unable to resume process ("
								+ processInstanceId
								+ "): the object is not WorkItemNodeInstance instance.");
				throw new Exception(
						"PROCESS RESUME - Unable to resume process ("
								+ processInstanceId
								+ "): the object is not WorkItemNodeInstance instance.");
			}

			if (workItemNodeInstance != null) {
				final long workItemId = workItemNodeInstance.getWorkItemId();
				final Map<String, Object> results = new HashMap<String, Object>();
				results.put("RedoTask", true);
				ksession.getWorkItemManager().completeWorkItem(workItemId,
						results);
			}
		} catch (Exception ex) {
			LOG.warn("PROCESS RESUME - Unable to resume process ("
					+ processInstanceId + "): " + ex.getMessage() + ".", ex);
			throw new Exception("PROCESS RESUME - Unable to resume process ("
					+ processInstanceId + "): " + ex.getMessage() + ".", ex);
		}
	}

}
