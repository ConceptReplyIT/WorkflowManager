package it.reply.workflowmanager.cdi.orchestrator.bpm.ejbcommands;

import it.reply.workflowmanager.cdi.orchestrator.annotations.ManageEntities;
import it.reply.workflowmanager.orchestrator.bpm.ejbcommands.AbstractBaseCommand;

import org.kie.api.executor.CommandContext;
import org.kie.api.executor.ExecutionResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

/**
 * Abstract base class for command implementation.<br/>
 * This sets up logger and some variables from the Executor command context. It also manages logging
 * at the start and end of command and provides helper methods for error and result handling.
 * 
 * @author l.biava
 * 
 */
@ManageEntities
public abstract class BaseCommand extends AbstractBaseCommand {

  private static final Logger LOG = LoggerFactory.getLogger(BaseCommand.class);

  @Override
  @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
  public ExecutionResults execute(CommandContext ctx) throws Exception {
    return super.execute(ctx);
  }

}
