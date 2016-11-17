package it.reply.workflowmanager.orchestrator.bpm.ejbcommands;

import org.kie.api.executor.CommandContext;
import org.kie.api.executor.ExecutionResults;

/**
 * Interface for retriable commands.
 * 
 * @author a.brigandi
 * 
 */
public interface RetriableCommand {

	/**
	 * Retry command's logic.
	 * 
	 * @param ctx
	 *            - contextual data given by the executor service
	 * @return returns any results in case of successful execution
	 * @throws Exception
	 */
	public ExecutionResults retry(CommandContext ctx) throws Exception;
	
	/**
	 * 
	 * @param ctx
	 *            - contextual data given by the executor service
	 * @return Max number of retries
	 */
	public int getNumOfMaxRetries(CommandContext ctx);

}
