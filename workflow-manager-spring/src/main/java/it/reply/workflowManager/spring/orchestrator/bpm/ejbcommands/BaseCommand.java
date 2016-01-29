package it.reply.workflowManager.spring.orchestrator.bpm.ejbcommands;

import it.reply.workflowManager.orchestrator.bpm.ejbcommands.AbstractBaseCommand;

import org.apache.logging.log4j.Logger;
import org.kie.api.executor.CommandContext;
import org.kie.api.executor.ExecutionResults;
import org.springframework.aop.framework.Advised;
import org.springframework.beans.factory.BeanNameAware;
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
public abstract class BaseCommand extends AbstractBaseCommand implements BeanNameAware {

  private static final Logger LOG = LogManager.getLogger(BaseCommand.class);

  @Autowired
  private ApplicationContext applicationContext;

  private BaseCommand self;

  private String beanName;

  @Override
  public void setBeanName(String name) {
    beanName = name;
  }

  @PostConstruct
  private void init() throws Exception {
    // if (this instanceof Advised) {
    // // is AOP proxied
    // self = (BaseCommand) ((Advised) this).getTargetSource().getTarget();
    // } else {
    self = applicationContext.getBean(this.getClass());
    // }
    // Object bean = applicationContext.getBean(beanName);
    //
    // self = (BaseCommand) asd;
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
