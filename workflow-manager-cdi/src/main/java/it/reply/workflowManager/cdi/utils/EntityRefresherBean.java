package it.reply.workflowManager.cdi.utils;

import it.reply.workflowManager.cdi.orchestrator.annotations.PlatformPersistenceUnit;
import it.reply.workflowManager.utils.AbstractEntityRefresher;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Stateless
public class EntityRefresherBean extends AbstractEntityRefresher {

  protected static Logger LOG = LoggerFactory.getLogger(EntityRefresherBean.class);

  @Inject
  @PlatformPersistenceUnit
  private EntityManager entityManager;

  @Override
  protected EntityManager getEntityManager() {
    return entityManager;
  }
}