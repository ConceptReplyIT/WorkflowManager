package it.reply.workflowmanager.spring.orchestrator.config;

import org.jbpm.process.audit.JPAWorkingMemoryDbLogger;
import org.jbpm.runtime.manager.impl.DefaultRegisterableItemsFactory;
import org.jbpm.runtime.manager.impl.RuntimeEngineImpl;
import org.kie.api.event.process.ProcessEventListener;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.executor.ExecutorService;
import org.kie.internal.runtime.manager.WorkItemHandlerProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Implementation of <code>RegisterableItemsFactory</code> dedicated to CDI environments that allows
 * us to get injections of following components:
 * <ul>
 * <li><code>ExternalTaskEventListener</code> - required bean</li>
 * <li><code>WorkItemHandlerProducer</code> - optional bean (0 or more)</li>
 * <li><code>EventListenerProducer<ProcessEventListener></code> - optional bean (0 or more)</li>
 * <li><code>EventListenerProducer<AgendaEventListener></code> - optional bean (0 or more)</li>
 * <li><code>EventListenerProducer<WorkingMemoryEventListener></code> - optional bean (0 or more)
 * </li>
 * <li><code>RuntimeFinder</code> - optional required only when single CDI bean is going to manage
 * many <code>RuntimeManager</code> instances</li>
 * </ul>
 * In addition to that, <code>AbstractAuditLogger</code> can be set after the bean has been injected
 * if the default is not sufficient. Although this factory extends
 * <code>DefaultRegisterableItemsFactory</code>, it will not use any of the listeners and handlers
 * that come from the super class. It relies mainly on CDI injections where the only exception from
 * this rule is <code>AbstractAuditLogger</code> <br/>
 * Even though this is a fully qualified bean for injection, it provides helper methods to build its
 * instances using <code>BeanManager</code> in case more independent instances are required.
 * <ul>
 * <li>getFactory(BeanManager, AbstractAuditLogger)</li>
 * <li>getFactory(BeanManager, AbstractAuditLogger, KieContainer, String)</li>
 * </ul>
 */
@Component
public class AutowireableRegisterableItemsFactory extends DefaultRegisterableItemsFactory {

  private static final Logger logger = LoggerFactory
      .getLogger(AutowireableRegisterableItemsFactory.class);

  @Autowired
  private ExecutorService executorService;

  @Autowired
  private WorkItemHandlerProducer[] workItemHandlerProducers;

  private boolean disableJpaAuditLoggers = true;

  @Override
  public List<ProcessEventListener> getProcessEventListeners(RuntimeEngine runtime) {
    List<ProcessEventListener> eventListeners = super.getProcessEventListeners(runtime);
    
    if (disableJpaAuditLoggers) {
      // remove JPA Audit loggers if present
      Iterator<ProcessEventListener> it = eventListeners.iterator();
      while (it.hasNext()) {
        ProcessEventListener listener = it.next();
        if (JPAWorkingMemoryDbLogger.class.isInstance(listener)) {
          it.remove();
        }
      }
    }
    
    return eventListeners;
  }


  @Override
  public Map<String, WorkItemHandler> getWorkItemHandlers(RuntimeEngine runtime) {
    Map<String, WorkItemHandler> handler = super.getWorkItemHandlers(runtime);
    RuntimeManager manager = ((RuntimeEngineImpl) runtime).getManager();
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("ksession", runtime.getKieSession());
    parameters.put("taskService", runtime.getTaskService());
    parameters.put("runtimeManager", manager);
    parameters.put("kieContainer", getRuntimeManager().getKieContainer());
    parameters.put("executorService", executorService);

    for (WorkItemHandlerProducer producer : workItemHandlerProducers) {
      try {
        handler.putAll(producer.getWorkItemHandlers(manager.getIdentifier(), parameters));
      } catch (Exception e) {
        // do nothing as work item handler is considered optional
        logger.warn("Exception while evalutating work item handler prodcuers {}", e.getMessage());
      }
    }
    return handler;
  }
}