package it.reply.workflowManager.spring.orchestrator.jms;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManagerFactory;
import org.jbpm.process.audit.jms.AsyncAuditLogReceiver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import it.reply.workflowManager.spring.orchestrator.annotations.WorkflowPersistenceUnit;

/**
 * Extension of default <code>AsyncAuditLogReceiver</code> that is MessageListener to allow entity
 * manager factory to be injected. This class shall be declared as the actual Audit log processor
 * for JMS environments - like MDB.
 */
@Component
public class AuditJMSProcessorBean extends AsyncAuditLogReceiver {

  @Autowired
  @WorkflowPersistenceUnit
  private EntityManagerFactory entityManagerFactory;

  public AuditJMSProcessorBean() {
    super(null);
  }

  @PostConstruct
  public void configure() {
    setEntityManagerFactory(entityManagerFactory);
  }
}