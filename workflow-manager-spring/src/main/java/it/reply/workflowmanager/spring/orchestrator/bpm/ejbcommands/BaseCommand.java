package it.reply.workflowmanager.spring.orchestrator.bpm.ejbcommands;

import it.reply.workflowmanager.orchestrator.bpm.ejbcommands.AbstractBaseCommand;

import org.kie.api.executor.CommandContext;
import org.kie.api.executor.ExecutionResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;

/**
 * Abstract base class for command implementation.<br/>
 * This sets up logger and some variables from the Executor command context. It also manages logging
 * at the start and end of command and provides helper methods for error and result handling.
 * 
 * @author l.biava
 * 
 */
// @ManageEntities
public abstract class BaseCommand<T extends BaseCommand<T>> extends AbstractBaseCommand<T> {

  @Autowired
  private ApplicationContext applicationContext;

  @SuppressWarnings("unchecked")
  @PostConstruct
  private void init() {
    setFacade((T) applicationContext.getBean(this.getClass()));
  }

  @Override
  @Transactional(propagation = Propagation.NOT_SUPPORTED)
  public ExecutionResults execute(CommandContext ctx) throws Exception {
    return super.execute(ctx);
  }

}
