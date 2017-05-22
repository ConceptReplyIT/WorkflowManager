package it.reply.workflowmanager.spring.orchestrator.bpm.commands;

import it.reply.workflowmanager.orchestrator.bpm.OrchestratorContext;
import it.reply.workflowmanager.orchestrator.bpm.commands.BaseDispatcherCommand;
import it.reply.workflowmanager.spring.orchestrator.bpm.OrchestratorContextBean;

public class SpringDispatcherCommand extends BaseDispatcherCommand {

  public SpringDispatcherCommand() throws Exception {
    super(OrchestratorContextBean.getBean(OrchestratorContext.class));
  }

}
