package it.reply.workflowManager.cdi.orchestrator.config.WIHproducers;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.executor.ExecutorService;
import it.reply.workflowManager.cdi.orchestrator.bpm.commands.EJBDispatcherCommand;
import it.reply.workflowManager.orchestrator.bpm.commands.DispatcherCommand;
import it.reply.workflowManager.orchestrator.config.WIHproducers.AbstractWorkItemHandlersProducer;

/**
 * Producer of {@link WorkItemHandler}s to bind jBPM task to commands.
 * 
 * @author l.biava
 *
 */
public class WorkItemHandlersProducer extends AbstractWorkItemHandlersProducer {

  private static final Logger LOG = LogManager.getLogger(WorkItemHandlersProducer.class);

  @Inject
  private ExecutorService executorService;

  protected Class<? extends DispatcherCommand> getDistpacherCommandClass() {
    return EJBDispatcherCommand.class;
  }

  protected ExecutorService getExecutorService() {
    return executorService;
  }

}
