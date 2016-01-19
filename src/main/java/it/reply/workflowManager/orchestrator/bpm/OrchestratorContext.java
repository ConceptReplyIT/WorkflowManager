package it.reply.workflowManager.orchestrator.bpm;

import it.reply.workflowManager.orchestrator.bpm.ejbcommands.IEJBCommand;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

/**
 * This class contains all EJB Commands and it's used to let jBPM executor's
 * commands rejoin CDI enviroment.
 * 
 * @author l.biava
 * 
 */
@ApplicationScoped
public class OrchestratorContext {
	
	@Inject
	private Instance<IEJBCommand> ejbCommands;

	public IEJBCommand getCommand(String className) throws ClassNotFoundException {
		Class<? extends IEJBCommand> commandClass = Class.forName(className).asSubclass(IEJBCommand.class);
		return getCommand(commandClass);
	}
	
	public IEJBCommand getCommand(Class<? extends IEJBCommand> commandClass) {
		// Check if the requested command class extends/implements another class in addition to BaseCommand or IEJBCommand.
		// In this case the command must have at least a local view.
		return ejbCommands.select(commandClass).get();
	}

}
