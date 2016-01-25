package it.reply.workflowManager.spring.orchestrator.bpm;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import it.reply.workflowManager.orchestrator.bpm.AbstractOrchestratorContext;
import it.reply.workflowManager.orchestrator.bpm.ejbcommands.IEJBCommand;

/**
 * This class contains all EJB Commands and it's used to let jBPM executor's commands rejoin CDI
 * enviroment.
 * 
 * @author l.biava
 * 
 */
// @Service
// public class OrchestratorContextBean extends AbstractOrchestratorContext {
//
// @Inject
// private Instance<IEJBCommand> ejbCommands;
//
// @Override
// public IEJBCommand getCommand(Class<? extends IEJBCommand> commandClass) {
// // Check if the requested command class extends/implements another class in addition to
// // BaseCommand or IEJBCommand.
// // In this case the command must have at least a local view.
// return ejbCommands.select(commandClass).get();
// }
//
// }
