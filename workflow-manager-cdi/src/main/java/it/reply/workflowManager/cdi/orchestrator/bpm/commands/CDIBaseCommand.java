package it.reply.workflowManager.cdi.orchestrator.bpm.commands;

import java.lang.annotation.Annotation;

import javax.enterprise.inject.spi.BeanManager;
import javax.naming.InitialContext;

import it.reply.workflowManager.orchestrator.bpm.OrchestratorContext;
import it.reply.workflowManager.orchestrator.bpm.commands.DispatcherCommand;
import it.reply.workflowManager.logging.CustomLogger;
import it.reply.workflowManager.logging.CustomLoggerFactory;

import org.jbpm.executor.cdi.CDIUtils;

/**
 * This class gets the reference to the OrchestratorContext to re-enable CDI in executor context.
 * jBPM Executor's command should subclass this.
 * 
 * @author l.biava
 * 
 */
public abstract class CDIBaseCommand implements DispatcherCommand {

  protected CustomLogger logger;
  protected OrchestratorContext orchestratorContext;

  public CDIBaseCommand() throws Exception {

    logger = CustomLoggerFactory.getLogger(this.getClass());
    try {
      orchestratorContext = getBean(OrchestratorContext.class);
      if (orchestratorContext == null)
        throw new Exception();
    } catch (Exception e) {
      logger
          .fatal("FATAL ERROR: Cannot have access to OrchestratorContext from " + this.getClass());
      throw new Exception(
          "FATAL ERROR: Cannot have access to OrchestratorContext from " + this.getClass(), e);
    }
  }

  protected <T> T getBean(Class<T> beanClass) throws Exception {
    return getBean(beanClass, (Annotation[]) null);
  }

  protected <T> T getBean(Class<T> beanClass, Annotation... bindings) throws Exception {
    InitialContext initialContext = new InitialContext();
    BeanManager beanManager = (BeanManager) initialContext.lookup("java:comp/BeanManager");
    if (bindings == null) {
      bindings = new Annotation[0];
    }
    return CDIUtils.createBean(beanClass, beanManager, bindings);
  }
}
