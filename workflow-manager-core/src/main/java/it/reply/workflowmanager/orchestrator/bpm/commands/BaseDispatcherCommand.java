package it.reply.workflowmanager.orchestrator.bpm.commands;

import com.google.common.base.Strings;

import it.reply.workflowmanager.orchestrator.bpm.OrchestratorContext;
import it.reply.workflowmanager.orchestrator.bpm.WIHs.EJBWorkItemHelper;
import it.reply.workflowmanager.orchestrator.bpm.ejbcommands.IEJBCommand;
import it.reply.workflowmanager.utils.Constants;

import org.kie.api.executor.CommandContext;
import org.kie.api.executor.ExecutionResults;
import org.kie.api.runtime.process.WorkItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseDispatcherCommand implements DispatcherCommand {

  private static final Logger LOG = LoggerFactory.getLogger(BaseDispatcherCommand.class);

  private OrchestratorContext orchestratorContext;

  public BaseDispatcherCommand(OrchestratorContext orchestratorContext) throws Exception {
    this.orchestratorContext = orchestratorContext;
    if (orchestratorContext == null) {
      LOG.error("Cannot have access to OrchestratorContext from " + this.getClass());
      throw new Exception(
          "FATAL ERROR: Cannot have access to OrchestratorContext from " + this.getClass());
    }
  }

  public OrchestratorContext getOrchestratorContext() {
    return orchestratorContext;
  }

  @Override
  public ExecutionResults dispatch(String eJBCommandClass, CommandContext ctx) throws Exception {
    IEJBCommand<?> ejbCommand = orchestratorContext.getCommand(eJBCommandClass);
    return ejbCommand.execute(ctx);
  }

  @Override
  public ExecutionResults execute(CommandContext ctx) throws Exception {
    WorkItem workItem = EJBWorkItemHelper.getWorkItem(ctx);
    boolean mdcSetted = false;
    try {

      if (Constants.ASYNC_WIH_NAME.equals(workItem.getName())) {
        // we must set the MDC only in async task
        EJBWorkItemHelper.initMdcFromCtx(ctx);
        mdcSetted = true;
      }

      String eJBCommandClass = (String) workItem.getParameter(Constants.EJB_COMMAND_CLASS);

      if (Strings.isNullOrEmpty(eJBCommandClass)) {
        LOG.warn("Executing dummy command because of empty {}", Constants.EJB_COMMAND_CLASS);
        return new ExecutionResults();
      }

      return dispatch(eJBCommandClass, ctx);
    } finally {
      if (mdcSetted) {
        EJBWorkItemHelper.mdcCleanUp(ctx);
      }
    }
  }
}
