package it.reply.workflowManager.spring.orchestrator.config.WIHproducers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import it.reply.workflowManager.orchestrator.bpm.commands.DispatcherCommand;
import it.reply.workflowManager.orchestrator.config.WIHproducers.AbstractWorkItemHandlersProducer;
import it.reply.workflowManager.spring.orchestrator.bpm.commands.SpringDispatcherCommand;

import org.kie.api.executor.ExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/***
 * Producer of{
 * 
 * @link WorkItemHandler}s to bind jBPM task to commands.
 *
 * @author l.biava
 *
 */
@Component
public class WorkItemHandlersProducer extends AbstractWorkItemHandlersProducer {

  private static final Logger LOG = LoggerFactory.getLogger(WorkItemHandlersProducer.class);

  @Autowired
  private ExecutorService executorService;

  @Override
  protected Class<? extends DispatcherCommand> getDistpacherCommandClass() {
    return SpringDispatcherCommand.class;
  }

  @Override
  protected ExecutorService getExecutorService() {
    return executorService;
  }

}
