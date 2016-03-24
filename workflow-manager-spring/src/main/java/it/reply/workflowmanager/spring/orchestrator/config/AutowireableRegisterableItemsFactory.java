package it.reply.workflowmanager.spring.orchestrator.config;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.drools.compiler.kie.builder.impl.KieContainerImpl;
import org.drools.compiler.kie.util.CDIHelper;
import org.drools.core.util.StringUtils;
import org.jbpm.process.audit.AbstractAuditLogger;
import org.jbpm.process.audit.AuditLoggerFactory;
import org.jbpm.process.audit.JPAWorkingMemoryDbLogger;
import org.jbpm.process.audit.event.AuditEventBuilder;
import org.jbpm.process.instance.event.listeners.TriggerRulesEventListener;
import org.jbpm.runtime.manager.api.qualifiers.Agenda;
import org.jbpm.runtime.manager.api.qualifiers.Process;
import org.jbpm.runtime.manager.api.qualifiers.Task;
import org.jbpm.runtime.manager.api.qualifiers.WorkingMemory;
import org.jbpm.runtime.manager.impl.DefaultRegisterableItemsFactory;
import org.jbpm.runtime.manager.impl.RuntimeEngineImpl;
import org.jbpm.runtime.manager.impl.jpa.EntityManagerFactoryManager;
import org.kie.api.builder.model.KieSessionModel;
import org.kie.api.event.process.ProcessEventListener;
import org.kie.api.event.rule.AgendaEventListener;
import org.kie.api.event.rule.RuleRuntimeEventListener;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.manager.RegisterableItemsFactory;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.task.TaskLifeCycleEventListener;
import org.kie.api.executor.ExecutorService;
import org.kie.internal.runtime.conf.AuditMode;
import org.kie.internal.runtime.conf.DeploymentDescriptor;
import org.kie.internal.runtime.manager.EventListenerProducer;
import org.kie.internal.runtime.manager.GlobalProducer;
import org.kie.internal.runtime.manager.WorkItemHandlerProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

///**
// * Implementation of <code>RegisterableItemsFactory</code> dedicated to CDI environments that allows
// * us to get injections of following components:
// * <ul>
// * <li><code>ExternalTaskEventListener</code> - required bean</li>
// * <li><code>WorkItemHandlerProducer</code> - optional bean (0 or more)</li>
// * <li><code>EventListenerProducer<ProcessEventListener>></code> - optional bean (0 or more)</li>
// * <li><code>EventListenerProducer<AgendaEventListener>></code> - optional bean (0 or more)</li>
// * <li><code>EventListenerProducer<WorkingMemoryEventListener>></code> - optional bean (0 or more)
// * </li>
// * <li><code>RuntimeFinder</code> - optional required only when single CDI bean is going to manage
// * many <code>RuntimeManager</code> instances</li>
// * </ul>
// * In addition to that, <code>AbstractAuditLogger</code> can be set after the bean has been injected
// * if the default is not sufficient. Although this factory extends
// * <code>DefaultRegisterableItemsFactory</code>, it will not use any of the listeners and handlers
// * that come from the super class. It relies mainly on CDI injections where the only exception from
// * this rule is <code>AbstractAuditLogger</code> <br/>
// * Even though this is a fully qualified bean for injection, it provides helper methods to build its
// * instances using <code>BeanManager</code> in case more independent instances are required.
// * <ul>
// * <li>getFactory(BeanManager, AbstractAuditLogger)</li>
// * <li>getFactory(BeanManager, AbstractAuditLogger, KieContainer, String)</li>
// * </ul>
// */
@Component
public class AutowireableRegisterableItemsFactory extends DefaultRegisterableItemsFactory {

  private static final Logger logger = LoggerFactory
      .getLogger(AutowireableRegisterableItemsFactory.class);

  @Autowired
  private ExecutorService executorService;

  @Autowired
  private ApplicationContext applicationContext;

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

    for (WorkItemHandlerProducer producer : applicationContext
        .getBeansOfType(WorkItemHandlerProducer.class).values()) {
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