package it.reply.workflowManager.spring.orchestrator.config.WIHproducers;

import it.reply.workflowManager.orchestrator.bpm.commands.DispatcherCommand;
import it.reply.workflowManager.orchestrator.config.WIHproducers.AbstractWorkItemHandlersProducer;
import it.reply.workflowManager.spring.orchestrator.bpm.commands.SpringDispatcherCommand;

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
  protected void setExecutorService(ExecutorService executorService) {
    super.setExecutorService(executorService);
  }

}
