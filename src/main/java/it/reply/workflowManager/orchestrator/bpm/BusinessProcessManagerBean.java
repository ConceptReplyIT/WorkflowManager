package it.reply.workflowManager.orchestrator.bpm;

import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Startup;
import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javatuples.Pair;
import org.jbpm.process.audit.AbstractAuditLogger;
import org.jbpm.process.audit.AuditLoggerFactory;
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
import org.kie.internal.runtime.manager.cdi.qualifier.PerProcessInstance;
import org.kie.internal.runtime.manager.cdi.qualifier.PerRequest;
import org.kie.internal.runtime.manager.cdi.qualifier.Singleton;
import org.kie.internal.runtime.manager.context.EmptyContext;
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;

/**
 * This class is the manager of Business Processes execution. It can be used to launch a new process
 * 
 * @author l.biava
 *
 */
@Startup
@javax.ejb.Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class BusinessProcessManagerBean implements BusinessProcessManager {

	public static Logger LOG = LogManager
			.getLogger(BusinessProcessManager.class);

	@Inject
	@Singleton
	private RuntimeManager singletonRuntimeManager;

	@Inject
	@PerProcessInstance
	private RuntimeManager perProcessInstanceRuntimeManager;

//	@Inject
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
			RuntimeEngine runtime = singletonRuntimeManager.getRuntimeEngine(ProcessInstanceIdContext.get());
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

	@Override
	public Pair<ProcessInstance, KieSession> startProcess(
			String procName, Map<String, Object> params,
			RUNTIME_STRATEGY runtimeStrat) throws Exception {

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

		ProcessInstance processInstance= ksession.createProcessInstance(procName, params);

		// Start process
		processInstance = ksession.startProcessInstance(processInstance.getId());

		return new Pair<ProcessInstance, KieSession>(processInstance,
				ksession);
	}

	private void registerjBPMAuditing(KieSession ksession) {
		AbstractAuditLogger auditLogger = AuditLoggerFactory.newInstance(AuditLoggerFactory.Type.JPA, ksession, null);

		ksession.addEventListener(auditLogger);

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