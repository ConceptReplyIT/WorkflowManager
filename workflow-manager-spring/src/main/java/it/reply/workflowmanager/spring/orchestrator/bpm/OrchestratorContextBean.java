package it.reply.workflowmanager.spring.orchestrator.bpm;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import it.reply.workflowmanager.orchestrator.bpm.AbstractOrchestratorContext;
import it.reply.workflowmanager.orchestrator.bpm.ejbcommands.IEJBCommand;

@Component
public class OrchestratorContextBean extends AbstractOrchestratorContext {

  private static final Logger LOG =
      LoggerFactory.getLogger(OrchestratorContextBean.class);
  
  private static ApplicationContext APPLICATION_CONTEXT;

  private static final ReentrantLock LOCK = new ReentrantLock();
  
  private static final Condition CONDITION = LOCK.newCondition();

  public static void setStaticApplicationContext(ApplicationContext applicationContext) {
    Objects.requireNonNull(applicationContext, "ApplicationContext must not be null");
    if (OrchestratorContextBean.APPLICATION_CONTEXT == null) {
      LOCK.lock();
      try {
        if (OrchestratorContextBean.APPLICATION_CONTEXT == null) {
          OrchestratorContextBean.APPLICATION_CONTEXT = applicationContext;
          CONDITION.signalAll();
          LOG.debug("Thread <{}> set ApplicationContext",
              Thread.currentThread().getName());
        }
      } finally {
        LOCK.unlock();
      }
    }
  }
  
  public static <T> T getBean(Class<T> beanClass) throws InterruptedException {
    if (OrchestratorContextBean.APPLICATION_CONTEXT == null) {
      LOCK.lock();
      try {
        while (OrchestratorContextBean.APPLICATION_CONTEXT == null) {
          LOG.debug("Thread <{}> waiting for ApplicationContext to be set",
              Thread.currentThread().getName());
            CONDITION.await(1, TimeUnit.SECONDS);
        }
      } finally {
        LOCK.unlock();
      }
    }
    return OrchestratorContextBean.APPLICATION_CONTEXT.getBean(beanClass);
  }
  
  private final ApplicationContext applicationContext;

  @Autowired
  public OrchestratorContextBean(ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
    OrchestratorContextBean.setStaticApplicationContext(applicationContext);
  }
  
  @Override
  public IEJBCommand getCommand(Class<? extends IEJBCommand> commandClass) {
    return applicationContext.getBean(commandClass);
  }

}
