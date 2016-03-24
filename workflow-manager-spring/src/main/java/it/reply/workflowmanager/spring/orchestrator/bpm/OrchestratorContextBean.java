package it.reply.workflowmanager.spring.orchestrator.bpm;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import it.reply.workflowmanager.orchestrator.bpm.AbstractOrchestratorContext;
import it.reply.workflowmanager.orchestrator.bpm.ejbcommands.IEJBCommand;

@Component
public class OrchestratorContextBean extends AbstractOrchestratorContext
    implements ApplicationContextAware {

  private static ApplicationContext APPLICATION_CONTEXT;

  private static ReadWriteLock LOCK = new ReentrantReadWriteLock();

  @Override
  public IEJBCommand getCommand(Class<? extends IEJBCommand> commandClass) {
    return OrchestratorContextBean.getCommandBean(commandClass);
  }

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    LOCK.writeLock().lock();
    try {
      OrchestratorContextBean.APPLICATION_CONTEXT = applicationContext;
    } finally {
      LOCK.writeLock().unlock();
    }
  }

  public static IEJBCommand getCommandBean(String commandClassName) throws ClassNotFoundException {
    Class<? extends IEJBCommand> commandClass = Class.forName(commandClassName)
        .asSubclass(IEJBCommand.class);
    return OrchestratorContextBean.getCommandBean(commandClass);
  }

  public static IEJBCommand getCommandBean(Class<? extends IEJBCommand> commandClass) {
    LOCK.readLock().lock();
    try {
      return OrchestratorContextBean.APPLICATION_CONTEXT.getBean(commandClass);
    } finally {
      LOCK.readLock().unlock();
    }
  }
}
