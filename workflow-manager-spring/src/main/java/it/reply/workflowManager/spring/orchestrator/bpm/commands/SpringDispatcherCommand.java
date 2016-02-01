package it.reply.workflowManager.spring.orchestrator.bpm.commands;

import it.reply.workflowManager.orchestrator.bpm.commands.DispatcherCommand;
import it.reply.workflowManager.orchestrator.bpm.ejbcommands.IEJBCommand;
import it.reply.workflowManager.spring.orchestrator.bpm.OrchestratorContextBean;
import it.reply.workflowManager.utils.Constants;

import org.kie.api.runtime.process.WorkItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

import org.kie.api.executor.CommandContext;
import org.kie.api.executor.ExecutionResults;

/**
 * This class implements a jBPM Executor Command which works as a dispatcher to load EJB commands.
 * <br/>
 * To successfully load (and execute) an EJB command it is required a parameter
 * <code>EJBCommandClass</code> containing the FQCN of the EJB command to load. <br/>
 * The EJB Command must also implement {@link IEJBCommand} interface.
 * 
 * @author l.biava
 * 
 */
public class SpringDispatcherCommand implements DispatcherCommand {

  private static final Logger LOG = LoggerFactory.getLogger(SpringDispatcherCommand.class);

  @Override
  public ExecutionResults execute(CommandContext ctx) throws Exception {

    WorkItem workItem = (WorkItem) ctx.getData(Constants.WORKITEM);
    String eJBCommandClass = (String) workItem.getParameter(Constants.EJB_COMMAND_CLASS);

    if (Strings.isNullOrEmpty(eJBCommandClass)) {
      LOG.warn("Executing dummy command because of empty {}", Constants.EJB_COMMAND_CLASS);
      return new ExecutionResults();
    }

    IEJBCommand ejbCommand = OrchestratorContextBean.getCommandBean(eJBCommandClass);

    return ejbCommand.execute(ctx);
  }

}
