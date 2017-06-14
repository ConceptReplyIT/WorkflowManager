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
  public <T extends IEJBCommand<T>> T getCommand(String className) throws ClassNotFoundException {
    Class<T> commandClass = (Class<T>) Class.forName(className);
    return getCommand(commandClass);
  }

}
