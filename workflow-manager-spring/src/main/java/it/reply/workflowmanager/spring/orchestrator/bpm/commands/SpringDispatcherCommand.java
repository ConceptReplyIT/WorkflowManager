package it.reply.workflowmanager.spring.orchestrator.bpm.commands;

import it.reply.workflowmanager.orchestrator.bpm.commands.BaseDispatcherCommand;
import it.reply.workflowmanager.orchestrator.bpm.ejbcommands.IEJBCommand;
import it.reply.workflowmanager.spring.orchestrator.bpm.OrchestratorContextBean;

import org.kie.api.executor.CommandContext;
import org.kie.api.executor.ExecutionResults;

public class SpringDispatcherCommand extends BaseDispatcherCommand {

  @Override
  public ExecutionResults dispatch(String eJBCommandClass, CommandContext ctx) throws Exception {
    IEJBCommand ejbCommand = OrchestratorContextBean.getCommandBean(eJBCommandClass);
    return ejbCommand.execute(ctx);
  }

}
