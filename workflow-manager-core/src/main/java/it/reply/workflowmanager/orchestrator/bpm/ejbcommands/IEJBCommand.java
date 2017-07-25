package it.reply.workflowmanager.orchestrator.bpm.ejbcommands;

import org.kie.api.executor.Command;

/**
 * All commands that are executed by jBPM executor MUST implement this interface (for EJB loading
 * purpose).
 * 
 * @author l.biava
 * 
 */
public interface IEJBCommand<T extends IEJBCommand<T>> extends Command {

}
