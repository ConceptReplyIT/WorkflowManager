package it.reply.workflowmanager.cdi.orchestrator.config.WIHproducers;

import it.reply.workflowmanager.cdi.orchestrator.bpm.commands.EJBDispatcherCommand;
import it.reply.workflowmanager.orchestrator.bpm.commands.DispatcherCommand;
import it.reply.workflowmanager.orchestrator.config.ConfigProducer;
import it.reply.workflowmanager.orchestrator.config.WIHproducers.AbstractWorkItemHandlersProducer;
import org.kie.api.executor.ExecutorService;
import org.kie.api.runtime.process.WorkItemHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

/**
 * Producer of {@link WorkItemHandler}s to bind jBPM task to commands.
 * 
 * @author l.biava
 *
 */
public class WorkItemHandlersProducer extends AbstractWorkItemHandlersProducer {

  private static final Logger LOG = LoggerFactory.getLogger(WorkItemHandlersProducer.class);

  @Override
  protected Class<? extends DispatcherCommand> getDistpacherCommandClass() {
    return EJBDispatcherCommand.class;
  }

  @Inject
  @Override
  protected void setExecutorService(ExecutorService executorService,
      ConfigProducer configProducer) {
    super.setExecutorService(executorService, configProducer);
  }

}
