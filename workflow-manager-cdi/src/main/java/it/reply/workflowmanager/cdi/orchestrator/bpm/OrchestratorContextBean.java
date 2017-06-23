package it.reply.workflowmanager.cdi.orchestrator.bpm;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.naming.InitialContext;

import it.reply.workflowmanager.orchestrator.bpm.AbstractOrchestratorContext;
import it.reply.workflowmanager.orchestrator.bpm.ejbcommands.IEJBCommand;

import org.jbpm.executor.cdi.CDIUtils;

import java.lang.annotation.Annotation;

/**
 * This class contains all EJB Commands and it's used to let jBPM executor's commands rejoin CDI
 * enviroment.
 * 
 * @author l.biava
 * 
 */
@ApplicationScoped
public class OrchestratorContextBean extends AbstractOrchestratorContext {

  @Inject
  private Instance<IEJBCommand<? extends IEJBCommand<?>>> ejbCommands;

  @Override
  public <T extends IEJBCommand<T>> T getCommand(Class<T> commandClass) {
    // Check if the requested command class extends/implements another class in addition to
    // BaseCommand or IEJBCommand.
    // In this case the command must have at least a local view.
    return ejbCommands.select(commandClass).get();
  }

  public static <T> T getBean(Class<T> beanClass) throws Exception {
    return getBean(beanClass, (Annotation[]) null);
  }

  public static <T> T getBean(Class<T> beanClass, Annotation... bindings) throws Exception {
    InitialContext initialContext = new InitialContext();
    BeanManager beanManager = (BeanManager) initialContext.lookup("java:comp/BeanManager");
    if (bindings == null) {
      bindings = new Annotation[0];
    }
    return CDIUtils.createBean(beanClass, beanManager, bindings);
  }

}
