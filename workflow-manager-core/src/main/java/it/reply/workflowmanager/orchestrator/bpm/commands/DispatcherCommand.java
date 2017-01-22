package it.reply.workflowmanager.orchestrator.bpm.commands;

import it.reply.workflowmanager.orchestrator.bpm.ejbcommands.IEJBCommand;
import org.kie.api.executor.Command;
import org.kie.api.executor.CommandContext;
import org.kie.api.executor.ExecutionResults;

/**
 * This class implements a jBPM Executor Command which works as a dispatcher to load EJB commands.
 * <br/>
 * To successfully load (and execute) an EJB command it is required a parameter
 * <code>EJBCommandClass</code> containing the FQCN of the EJB command to load. <br/>
 * The EJB Command must also implement {@link IEJBCommand} interface.
 * 
 * @author l.biava
 * 
 */
public interface DispatcherCommand extends Command {

  public ExecutionResults dispatch(String eJBCommandClass, CommandContext ctx) throws Exception;
}
