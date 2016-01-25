package it.reply.workflowManager.orchestrator.bpm;

import it.reply.workflowManager.orchestrator.bpm.ejbcommands.IEJBCommand;

/**
 * This class contains all EJB Commands and it's used to let jBPM executor's commands rejoin CDI
 * enviroment.
 * 
 * @author l.biava
 * 
 */
public interface OrchestratorContext {

  public IEJBCommand getCommand(String className) throws ClassNotFoundException;

  public IEJBCommand getCommand(Class<? extends IEJBCommand> commandClass);

}
