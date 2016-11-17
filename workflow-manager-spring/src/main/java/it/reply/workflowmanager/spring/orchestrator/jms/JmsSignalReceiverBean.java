package it.reply.workflowmanager.spring.orchestrator.jms;

import org.jbpm.process.workitem.jms.JMSSignalReceiver;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.Message;

@Component
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class JmsSignalReceiverBean extends JMSSignalReceiver {

  // Must run outside of a transaction context
  @Override
  @Transactional(propagation = Propagation.NOT_SUPPORTED)
  public void onMessage(Message message) {
    super.onMessage(message);
  }
}
