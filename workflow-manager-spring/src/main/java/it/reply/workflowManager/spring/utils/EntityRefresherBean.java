package it.reply.workflowManager.spring.utils;

import it.reply.workflowManager.spring.orchestrator.annotations.PlatformPersistenceUnit;
import it.reply.workflowManager.utils.AbstractEntityRefresher;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

@Service
public class EntityRefresherBean extends AbstractEntityRefresher {

  protected static Logger LOG = LogManager.getLogger(EntityRefresherBean.class);

  @Resource
  @PlatformPersistenceUnit
  private EntityManager entityManager;

  @Override
  protected EntityManager getEntityManager() {
    return entityManager;
  }
}