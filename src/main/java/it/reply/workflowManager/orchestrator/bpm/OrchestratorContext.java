package it.reply.workflowManager.orchestrator.bpm;

import it.reply.workflowManager.orchestrator.bpm.ejbcommands.IEJBCommand;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

/**
 * This class contains all EJB Commands and it's used to let jBPM executor's
 * commands rejoin CDI enviroment.
 * 
 * @author l.biava
 * 
 */
@Singleton
@Startup
public class OrchestratorContext {
	
	@Inject
	private Instance<IEJBCommand> ejbCommands;

	public IEJBCommand getCommand(String className) throws ClassNotFoundException {
		// Check if the requested command class extends/implements another class in addition to BaseCommand or IEJBCommand.
		// In this case the command must have at least a local view.
		return ejbCommands.select(Class.forName(className).asSubclass(IEJBCommand.class)).get();
	}
	
	@PostConstruct
	public void init() {
		System.out.println("OrchestratorContext Alive !");
		System.out.flush();
	}
}
