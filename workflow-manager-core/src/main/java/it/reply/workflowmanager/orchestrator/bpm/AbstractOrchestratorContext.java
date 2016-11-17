package it.reply.workflowmanager.orchestrator.bpm;

import it.reply.workflowmanager.orchestrator.bpm.ejbcommands.IEJBCommand;

/**
 * This class contains all EJB Commands and it's used to let jBPM executor's commands rejoin CDI
 * enviroment.
 * 
 * @author l.biava
 * 
 */
public abstract class AbstractOrchestratorContext implements OrchestratorContext {

  @Override
  public IEJBCommand getCommand(String className) throws ClassNotFoundException {
    Class<? extends IEJBCommand> commandClass = Class.forName(className)
        .asSubclass(IEJBCommand.class);
    return getCommand(commandClass);
  }

}
