package it.reply.workflowmanager.spring.utils;

import it.reply.workflowmanager.utils.AbstractEntityRefresher;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class EntityRefresherBean extends AbstractEntityRefresher {

  protected static Logger LOG = LoggerFactory.getLogger(EntityRefresherBean.class);

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