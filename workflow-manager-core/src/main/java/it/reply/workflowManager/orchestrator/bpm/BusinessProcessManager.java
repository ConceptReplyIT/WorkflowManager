package it.reply.workflowManager.orchestrator.bpm;

import java.util.Map;

import org.javatuples.Pair;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.ProcessInstance;

/**
 * This class is the manager of Business Processes execution. It can be used to launch a new process
 * 
 * @author l.biava
 *
 */
public interface BusinessProcessManager {

	public enum RUNTIME_STRATEGY {
		SINGLETON, PER_PROCESS_INSTANCE, PER_REQUEST
	}

	public RuntimeManager getSingletonRuntimeManager();

	public RuntimeManager getPerProcessInstanceRuntimeManager();

	public RuntimeManager getPerRequestRuntimeManager();

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
			RUNTIME_STRATEGY runtimeStrat) throws Exception;

}
