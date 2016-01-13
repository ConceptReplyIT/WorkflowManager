package it.reply.workflowManager.orchestrator.config;

import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceUnit;
import javax.transaction.TransactionManager;

import org.jbpm.services.cdi.impl.manager.InjectableRegisterableItemsFactory;
import org.kie.api.executor.ExecutorService;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.EnvironmentName;
import org.kie.api.runtime.manager.RuntimeEnvironment;
import org.kie.api.runtime.manager.RuntimeEnvironmentBuilder;
import org.kie.api.task.UserGroupCallback;
import org.kie.internal.io.ResourceFactory;
import org.kie.internal.runtime.manager.cdi.qualifier.PerProcessInstance;
import org.kie.internal.runtime.manager.cdi.qualifier.PerRequest;
import org.kie.internal.runtime.manager.cdi.qualifier.Singleton;

@ApplicationScoped
/**
 * Used to configure jBPM Environment (persistence config, loading BPMN assets, binding WIH, ecc.) and the rest of the application.
 * @author l.biava
 *
 */
public class CustomApplicationScopedProducer {

	/*
	 * @Inject //@Qualifier(
	 * "org.jbpm.examples.workitems.producers.RestTaskWorkItemHandlersProducer")
	 * private ApplicationWorkItemHandlersProducer wihProducer;
	 */

	@Inject
	private InjectableRegisterableItemsFactory factory;

	@Inject
	private UserGroupCallback usergroupCallback;

//	@Inject
//	private ExecutorService executorService;
	
//	@PersistenceContext(unitName = "org.jbpm.domain")
//	private EntityManager emPrisma;
//
//	@PostConstruct
//	private void init() {
//		emPrisma.joinTransaction();
//	}

	@PersistenceUnit(unitName = "org.jbpm.domain")
	private EntityManagerFactory emf;
	
//	@PersistenceUnit(unitName = "PrismaDAL")
//	private EntityManagerFactory emfPrisma;

	@Resource(mappedName = "java:jboss/TransactionManager")
	private TransactionManager tm;

	@Produces
	public EntityManagerFactory produceEntityManagerFactory() {
//		if (this.emf == null) {
//			this.emf = Persistence.createEntityManagerFactory("org.jbpm.domain");
//		}
		return this.emf;
	}
    
//	@Singleton
//	@Produces
//	public RuntimeManager produceSingletonRuntimeManager(
//			@Singleton RuntimeEnvironment environment) {
//		// next create RuntimeManager - in this case singleton strategy is
//		// chosen
//		return RuntimeManagerFactory.Factory.get().newSingletonRuntimeManager(
//				environment);
//	}

	@Produces
	@Singleton
	@PerProcessInstance
	@PerRequest
	public RuntimeEnvironment produceEnvironment(EntityManagerFactory emf) {
		RuntimeEnvironment environment = RuntimeEnvironmentBuilder.Factory
				.get()
				.newDefaultBuilder()
				.entityManagerFactory(emf)
				.userGroupCallback(usergroupCallback)
				.registerableItemsFactory(factory)
				// .registerableItemsFactory(new
				// InjectableRegisterableItemsFactory())
				// TEST per soluzione problema persistenza e ripristino !
				.persistence(true)
				.addEnvironmentEntry(EnvironmentName.TRANSACTION_MANAGER, tm)
//				.addEnvironmentEntry("ExecutorService", executorService)
//				.addEnvironmentEntry(EnvironmentName.OBJECT_MARSHALLING_STRATEGIES, 
//						new ObjectMarshallingStrategy[] {
//							new CustomJPAPlaceholderResolverStrategy(emfPrisma),
//							new SerializablePlaceholderResolverStrategy( 
//		                          ClassObjectMarshallingStrategyAcceptor.DEFAULT  )
//		         })  
						
				
				// .addAsset(
				// ResourceFactory
				// .newClassPathResource("business-processes/misc/dummy.bpmn"),
				// ResourceType.BPMN2)
				.addAsset(
						ResourceFactory
								.newClassPathResource("business-processes/deploy/cloudify/cloudify-paas-deploy.bpmn"),
						ResourceType.BPMN2)
				.addAsset(
						ResourceFactory
								.newClassPathResource("business-processes/deploy/heat/heat-paas-deploy.bpmn"),
						ResourceType.BPMN2)
				.addAsset(
						ResourceFactory
								.newClassPathResource("business-processes/services/paas-provisioning.bpmn"),
						ResourceType.BPMN2)
				.addAsset(
						ResourceFactory
								.newClassPathResource("business-processes/deploy/cloudify/cloudify-paas-undeploy.bpmn"),
						ResourceType.BPMN2)
				.addAsset(
						ResourceFactory
								.newClassPathResource("business-processes/deploy/heat/heat-paas-undeploy.bpmn"),
						ResourceType.BPMN2)
				.addAsset(
						ResourceFactory
								.newClassPathResource("business-processes/services/paas-unprovisioning.bpmn"),
						ResourceType.BPMN2)
				.addAsset(
						ResourceFactory
								.newClassPathResource("business-processes/deploy/heat/heat-paas-manage.bpmn"),
						ResourceType.BPMN2)
				.addAsset(
						ResourceFactory
								.newClassPathResource("business-processes/services/paas-management.bpmn"),
						ResourceType.BPMN2)
						.get();
//		environment.getEnvironment().set(
//				EnvironmentName.USE_LOCAL_TRANSACTIONS, true);
		environment.getEnvironment().set(
				EnvironmentName.USE_PESSIMISTIC_LOCKING, true);
		// environment.set(EnvironmentName.PERSISTENCE_CONTEXT_MANAGER, new
		// JpaProcessPersistenceContextManager(environment));

		return environment;
	}

	public RuntimeEnvironment produceEnvironmentForTest() {
		RuntimeEnvironment environment = RuntimeEnvironmentBuilder.Factory
				.get()
				.newDefaultBuilder()
				.userGroupCallback(usergroupCallback)
				.registerableItemsFactory(factory)
				// .registerableItemsFactory(new
				// InjectableRegisterableItemsFactory())
				// TEST per soluzione problema persistenza e ripristino !
				.persistence(false)
				// new ContainerManagedTransactionManager())
				.addAsset(
						ResourceFactory
								.newClassPathResource("business-processes/rest-task.bpmn"),
						ResourceType.BPMN2)
				.addAsset(
						ResourceFactory
								.newClassPathResource("business-processes/lrt-provisioning-pentaho.bpmn"),
						ResourceType.BPMN2)
				.addAsset(
						ResourceFactory
								.newClassPathResource("business-processes/lrt-provisioning-dbaas.bpmn"),
						ResourceType.BPMN2)
				.addAsset(
						ResourceFactory
								.newClassPathResource("business-processes/services/APPaaS/proto-provisioning.bpmn"),
						ResourceType.BPMN2)
				.addAsset(
						ResourceFactory
								.newClassPathResource("business-processes/monitoring/checkmonitoring.bpmn"),
						ResourceType.BPMN2)

				.get();
		// environment.set(EnvironmentName.PERSISTENCE_CONTEXT_MANAGER, new
		// JpaProcessPersistenceContextManager(environment));
		return environment;
	}

}
