package it.reply.workflowmanager.cdi.orchestrator.bpm.commands;

import it.reply.workflowmanager.cdi.orchestrator.bpm.OrchestratorContextBean;
import it.reply.workflowmanager.orchestrator.bpm.OrchestratorContext;
import it.reply.workflowmanager.orchestrator.bpm.commands.BaseDispatcherCommand;

/**
 * This class gets the reference to the OrchestratorContext to re-enable CDI in executor context.
 * jBPM Executor's command should subclass this.
 * 
 * @author l.biava
 * 
 */
public class CDIDispatcherCommand extends BaseDispatcherCommand {

  public CDIDispatcherCommand() throws Exception {
    super(OrchestratorContextBean.getBean(OrchestratorContext.class));
  }
}
