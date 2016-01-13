package it.reply.workflowManager.orchestrator.bpm.commands;

import java.lang.annotation.Annotation;

import javax.enterprise.inject.spi.BeanManager;
import javax.naming.InitialContext;

import it.reply.workflowManager.orchestrator.bpm.OrchestratorContext;
import it.reply.workflowManager.logging.CustomLogger;
import it.reply.workflowManager.logging.CustomLoggerFactory;

import org.jbpm.executor.cdi.CDIUtils;
import org.kie.api.executor.Command;

/**
 * This class gets the reference to the OrchestratorContext to re-enable CDI in
 * executor context. jBPM Executor's command should subclass this.
 * 
 * @author l.biava
 * 
 */
public abstract class CDIBaseCommand implements Command {

	protected CustomLogger logger;
	protected OrchestratorContext orchestratorContext;

	public CDIBaseCommand() throws Exception {

		logger = CustomLoggerFactory.getLogger(this.getClass());
		try {
			InitialContext initialContext = new InitialContext();
			BeanManager beanManager = (BeanManager) initialContext.lookup("java:comp/BeanManager");
			orchestratorContext = CDIUtils.createBean(OrchestratorContext.class, beanManager, new Annotation[0]);
			if (orchestratorContext == null)
				throw new Exception();
		} catch (Exception e) {
			logger.fatal("FATAL ERROR: Cannot have access to OrchestratorContext from "
					+ this.getClass());
			throw new Exception(
					"FATAL ERROR: Cannot have access to OrchestratorContext from "
							+ this.getClass(), e);
		}
	}

}
