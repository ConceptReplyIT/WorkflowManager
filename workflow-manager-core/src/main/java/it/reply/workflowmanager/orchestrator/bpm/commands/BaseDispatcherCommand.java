package it.reply.workflowmanager.orchestrator.bpm.commands;

import it.reply.workflowmanager.orchestrator.bpm.WIHs.EJBWorkItemHelper;
import it.reply.workflowmanager.utils.Constants;

import org.kie.api.runtime.process.WorkItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

import org.kie.api.executor.CommandContext;
import org.kie.api.executor.ExecutionResults;

public abstract class BaseDispatcherCommand implements DispatcherCommand {

  private static final Logger LOG = LoggerFactory.getLogger(BaseDispatcherCommand.class);

  @Override
  public ExecutionResults execute(CommandContext ctx) throws Exception {
    WorkItem workItem = EJBWorkItemHelper.getWorkItem(ctx);
    try {
      
      if (Constants.ASYNC_WIH_NAME.equals(workItem.getName())) {
        // we must set the MDC only in async task
        EJBWorkItemHelper.initMdcFromCtx(ctx);
      }
      
      String eJBCommandClass = (String) workItem.getParameter(Constants.EJB_COMMAND_CLASS);
  
      if (Strings.isNullOrEmpty(eJBCommandClass)) {
        LOG.warn("Executing dummy command because of empty {}", Constants.EJB_COMMAND_CLASS);
        return new ExecutionResults();
      }

      return dispatch(eJBCommandClass, ctx);
    } catch (Exception ex) {
      if (workItem != null && Constants.ASYNC_WIH_NAME.equals(workItem.getName())) {
        LOG.error("Error executing command", ex);
        EJBWorkItemHelper.mdcCleanUp(ctx);
        // TODO should we handle the exception in some way?
      }
      throw ex;
    }
  }
}
