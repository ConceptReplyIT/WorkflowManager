package it.reply.workflowmanager.spring.orchestrator.jms;

import it.reply.workflowmanager.spring.orchestrator.annotations.WorkflowPersistenceUnit;

import org.jbpm.process.audit.jms.AsyncAuditLogReceiver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.jms.Message;
import javax.persistence.EntityManagerFactory;

/**
 * Extension of default <code>AsyncAuditLogReceiver</code> that is MessageListener to allow entity
 * manager factory to be injected. This class shall be declared as the actual Audit log processor
 * for JMS environments - like MDB.
 */
@Component
@Transactional
public class AuditJmsProcessorBean extends AsyncAuditLogReceiver {

  @Autowired
  @WorkflowPersistenceUnit
  private EntityManagerFactory entityManagerFactory;

  public AuditJmsProcessorBean() {
    super(null);
  }

  @PostConstruct
  public void configure() {
    setEntityManagerFactory(entityManagerFactory);
  }

  @Override
  @Transactional
  public void onMessage(Message message) {
    super.onMessage(message);
  }
}