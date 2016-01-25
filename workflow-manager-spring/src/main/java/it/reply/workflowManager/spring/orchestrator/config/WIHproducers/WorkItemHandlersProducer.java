package it.reply.workflowManager.spring.orchestrator.config.WIHproducers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import it.reply.workflowManager.orchestrator.bpm.commands.DispatcherCommand;
import it.reply.workflowManager.orchestrator.config.WIHproducers.AbstractWorkItemHandlersProducer;
import it.reply.workflowManager.spring.orchestrator.bpm.commands.SpringDispatcherCommand;

import org.kie.api.executor.ExecutorService;

/***
 * Producer of{
 * 
 * @link WorkItemHandler}s to bind jBPM task to commands.
 *
 * @author l.biava
 *
 */
public class WorkItemHandlersProducer extends AbstractWorkItemHandlersProducer {

  private static final Logger LOG = LogManager.getLogger(WorkItemHandlersProducer.class);

  @Autowired
  private ExecutorService executorService;

  protected Class<? extends DispatcherCommand> getDistpacherCommandClass() {
    return SpringDispatcherCommand.class;
  }

  protected ExecutorService getExecutorService() {
    return executorService;
  }

}
