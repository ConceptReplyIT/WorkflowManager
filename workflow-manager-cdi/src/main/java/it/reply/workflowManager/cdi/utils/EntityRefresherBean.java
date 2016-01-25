package it.reply.workflowManager.cdi.utils;

import it.reply.workflowManager.cdi.orchestrator.annotations.PlatformPersistenceUnit;
import it.reply.workflowManager.utils.AbstractEntityRefresher;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Stateless
public class EntityRefresherBean extends AbstractEntityRefresher {

  protected static Logger LOG = LogManager.getLogger(EntityRefresherBean.class);

  @Inject
  @PlatformPersistenceUnit
  private EntityManager entityManager;

  @Override
  protected EntityManager getEntityManager() {
    return entityManager;
  }
}