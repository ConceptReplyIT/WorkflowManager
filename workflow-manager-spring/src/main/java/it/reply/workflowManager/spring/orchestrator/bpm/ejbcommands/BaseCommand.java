package it.reply.workflowManager.spring.orchestrator.bpm.ejbcommands;

import it.reply.workflowManager.orchestrator.bpm.ejbcommands.AbstractBaseCommand;

import org.apache.logging.log4j.Logger;
import org.kie.api.executor.CommandContext;
import org.kie.api.executor.ExecutionResults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;

/**
 * Abstract base class for command implementation.<br/>
 * This sets up logger and some variables from the Executor command context. It also manages logging
 * at the start and end of command and provides helper methods for error and result handling.
 * 
 * @author l.biava
 * 
 */
// @ManageEntities
public abstract class BaseCommand extends AbstractBaseCommand {

  private static final Logger LOG = LogManager.getLogger(BaseCommand.class);

  @Autowired
  private ApplicationContext applicationContext;

  private BaseCommand self;

  @PostConstruct
  private void init() throws Exception {
    self = applicationContext.getBean(this.getClass());
  }

  @Override
  protected AbstractBaseCommand getFacade() {
    return self;
  }

  @Override
  @Transactional(propagation = Propagation.NOT_SUPPORTED)
  public ExecutionResults execute(CommandContext ctx) throws Exception {
    return super.execute(ctx);
  }

}
