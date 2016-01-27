package it.reply.workflowManager.spring.utils;

import it.reply.workflowManager.utils.AbstractEntityRefresher;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class EntityRefresherBean extends AbstractEntityRefresher {

  protected static Logger LOG = LogManager.getLogger(EntityRefresherBean.class);

  @Autowired
  @Qualifier("entityManagerFactory")
  private EntityManagerFactory entityManagerFactory;

  private EntityManager entityManager;

  @PostConstruct
  public void init() {
    entityManager = entityManagerFactory.createEntityManager();
  }

  @Override
  protected EntityManager getEntityManager() {
    return entityManager;
  }
}