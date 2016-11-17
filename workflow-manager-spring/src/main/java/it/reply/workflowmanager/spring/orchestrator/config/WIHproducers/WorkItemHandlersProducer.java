package it.reply.workflowmanager.spring.orchestrator.config.WIHproducers;

import it.reply.workflowmanager.orchestrator.bpm.commands.DispatcherCommand;
import it.reply.workflowmanager.orchestrator.config.ConfigProducer;
import it.reply.workflowmanager.orchestrator.config.WIHproducers.AbstractWorkItemHandlersProducer;
import it.reply.workflowmanager.spring.orchestrator.bpm.commands.SpringDispatcherCommand;

import org.kie.api.executor.ExecutorService;
import org.kie.api.runtime.process.WorkItemHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Producer of{@link WorkItemHandler}s to bind jBPM task to commands.
 *
 * @author l.biava
 *
 */
@Component
public class WorkItemHandlersProducer extends AbstractWorkItemHandlersProducer {

  private static final Logger LOG = LoggerFactory.getLogger(WorkItemHandlersProducer.class);

  @Override
  protected Class<? extends DispatcherCommand> getDistpacherCommandClass() {
    return SpringDispatcherCommand.class;
  }

  @Autowired
  @Override
  protected void setExecutorService(ExecutorService executorService,
      ConfigProducer configProducer) {
    super.setExecutorService(executorService, configProducer);
  }

}
