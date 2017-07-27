package it.reply.workflowmanager.spring.orchestrator.bpm;

import java.util.Objects;
import java.util.concurrent.CountDownLatch;

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
  
  private static final CountDownLatch LATCH = new CountDownLatch(1);
  
  private static ApplicationContext APPLICATION_CONTEXT;

  private final ApplicationContext applicationContext;
  
  @Autowired
  public OrchestratorContextBean(ApplicationContext applicationContext) {
    this.applicationContext = Objects.requireNonNull(applicationContext, "Application Context must not be null");
    APPLICATION_CONTEXT = applicationContext;
    LATCH.countDown();
    LOG.debug("ApplicationContext set by thread <{}>", Thread.currentThread());
  }
  
  public static <T> T getBean(Class<T> beanClass) throws InterruptedException {
    LATCH.await();
    return APPLICATION_CONTEXT.getBean(beanClass);
  }
  
  @Override
  public <T extends IEJBCommand<T>> T getCommand(Class<T> commandClass) {
    return applicationContext.getBean(commandClass);
  }

}
