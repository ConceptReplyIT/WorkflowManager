/*
 * Copyright 2013 JBoss by Red Hat.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * Modifications Copyright (C) 2016 Concept Reply
 *  
 */

package it.reply.workflowmanager.spring.orchestrator.config;

import it.reply.workflowmanager.spring.orchestrator.annotations.WorkflowPersistenceUnit;

import org.jbpm.executor.ExecutorServiceFactory;
import org.jbpm.executor.impl.ClassCacheManager;
import org.jbpm.executor.impl.jpa.ExecutorQueryServiceImpl;
import org.jbpm.executor.impl.jpa.ExecutorRequestAdminServiceImpl;
import org.jbpm.executor.impl.jpa.JPAExecutorStoreService;
import org.kie.api.executor.ExecutorAdminService;
import org.kie.api.executor.ExecutorQueryService;
import org.kie.api.executor.ExecutorService;
import org.kie.api.executor.ExecutorStoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;

@Configuration
@EnableTransactionManagement(proxyTargetClass = true)
public class JPAExecutorServiceProducer {

  @Autowired
  @WorkflowPersistenceUnit
  private EntityManagerFactory emf;

  @Bean
  public ExecutorService produceExecutorService() {
    ExecutorService service = ExecutorServiceFactory.newExecutorService(emf);

    return service;
  }

  @Bean
  public ExecutorStoreService produceStoreService() {
    ExecutorStoreService storeService = new JPAExecutorStoreService(true);
    org.jbpm.shared.services.impl.TransactionalCommandService commandService =
        new org.jbpm.shared.services.impl.TransactionalCommandService(emf);
    ((JPAExecutorStoreService) storeService).setCommandService(commandService);
    ((JPAExecutorStoreService) storeService).setEmf(emf);

    return storeService;
  }

  @Bean
  public ExecutorAdminService produceAdminService() {
    ExecutorAdminService adminService = new ExecutorRequestAdminServiceImpl();
    org.jbpm.shared.services.impl.TransactionalCommandService commandService =
        new org.jbpm.shared.services.impl.TransactionalCommandService(emf);
    ((ExecutorRequestAdminServiceImpl) adminService).setCommandService(commandService);

    return adminService;
  }

  @Bean
  public ExecutorQueryService produceQueryService() {
    ExecutorQueryService queryService = new ExecutorQueryServiceImpl(true);
    org.jbpm.shared.services.impl.TransactionalCommandService commandService =
        new org.jbpm.shared.services.impl.TransactionalCommandService(emf);
    ((ExecutorQueryServiceImpl) queryService).setCommandService(commandService);

    return queryService;
  }

  @Bean(destroyMethod = "dispose")
  public ClassCacheManager classCacheManager() {
    return new ClassCacheManager();
  }

}
