package it.reply.workflowmanager.cdi.orchestrator.bpm.commands;

import it.reply.workflowmanager.cdi.orchestrator.bpm.OrchestratorContextBean;
import it.reply.workflowmanager.orchestrator.bpm.OrchestratorContext;
import it.reply.workflowmanager.orchestrator.bpm.commands.BaseDispatcherCommand;
import it.reply.workflowmanager.orchestrator.bpm.ejbcommands.IEJBCommand;

import org.kie.api.executor.CommandContext;
import org.kie.api.executor.ExecutionResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class gets the reference to the OrchestratorContext to re-enable CDI in executor context.
 * jBPM Executor's command should subclass this.
 * 
 * @author l.biava
 * 
 */
public class CDIDispatcherCommand extends BaseDispatcherCommand {

  private static final Logger LOG = LoggerFactory.getLogger(CDIDispatcherCommand.class);

  private OrchestratorContext orchestratorContext;

  public CDIDispatcherCommand() throws Exception {

    orchestratorContext = OrchestratorContextBean.getBean(OrchestratorContext.class);
    if (orchestratorContext == null) {
      LOG.error("Cannot have access to OrchestratorContext from " + this.getClass());
      throw new Exception(
          "FATAL ERROR: Cannot have access to OrchestratorContext from " + this.getClass());
    }
  }

  @Override
  public ExecutionResults dispatch(String eJBCommandClass, CommandContext ctx) throws Exception {
    IEJBCommand ejbCommand = orchestratorContext.getCommand(eJBCommandClass);
    return ejbCommand.execute(ctx);
  }
}
