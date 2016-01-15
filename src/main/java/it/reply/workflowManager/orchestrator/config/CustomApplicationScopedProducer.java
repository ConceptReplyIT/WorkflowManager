package it.reply.workflowManager.orchestrator.config;

import java.util.List;

import it.reply.workflowManager.utils.Constants;

import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import javax.transaction.TransactionManager;

import org.javatuples.Pair;
import org.jbpm.runtime.manager.impl.SimpleRuntimeEnvironment;
import org.jbpm.services.cdi.impl.manager.InjectableRegisterableItemsFactory;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.EnvironmentName;
import org.kie.api.runtime.manager.RuntimeEnvironment;
import org.kie.api.runtime.manager.RuntimeEnvironmentBuilder;
import org.kie.api.task.UserGroupCallback;
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

	@Inject
	private InjectableRegisterableItemsFactory factory;

	@Inject
	private UserGroupCallback usergroupCallback;

//	@Inject
//	private ExecutorService executorService;

	@PersistenceUnit(unitName = Constants.PERSISTENCE_UNIT_NAME)
	private EntityManagerFactory emf;

	@Resource(mappedName = "java:jboss/TransactionManager")
	private TransactionManager tm;

	@Produces
	public EntityManagerFactory produceEntityManagerFactory() {
		return this.emf;
	}

	@Inject
	private List<Pair<org.kie.api.io.Resource, ResourceType>> resources;
	

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
				.persistence(true)
				.addEnvironmentEntry(EnvironmentName.TRANSACTION_MANAGER, tm)
//				.addEnvironmentEntry("ExecutorService", executorService)
//				.addAsset(
//						ResourceFactory
//								.newClassPathResource("business-processes/deploy/cloudify/cloudify-paas-deploy.bpmn"),
//						ResourceType.BPMN2)
//				.addAsset(
//						ResourceFactory
//								.newClassPathResource("business-processes/deploy/heat/heat-paas-deploy.bpmn"),
//						ResourceType.BPMN2)
//				.addAsset(
//						ResourceFactory
//								.newClassPathResource("business-processes/services/paas-provisioning.bpmn"),
//						ResourceType.BPMN2)
//				.addAsset(
//						ResourceFactory
//								.newClassPathResource("business-processes/deploy/cloudify/cloudify-paas-undeploy.bpmn"),
//						ResourceType.BPMN2)
//				.addAsset(
//						ResourceFactory
//								.newClassPathResource("business-processes/deploy/heat/heat-paas-undeploy.bpmn"),
//						ResourceType.BPMN2)
//				.addAsset(
//						ResourceFactory
//								.newClassPathResource("business-processes/services/paas-unprovisioning.bpmn"),
//						ResourceType.BPMN2)
//				.addAsset(
//						ResourceFactory
//								.newClassPathResource("business-processes/deploy/heat/heat-paas-manage.bpmn"),
//						ResourceType.BPMN2)
//				.addAsset(
//						ResourceFactory
//								.newClassPathResource("business-processes/services/paas-management.bpmn"),
//						ResourceType.BPMN2)
						.get();
		for (Pair<org.kie.api.io.Resource, ResourceType> resource : resources) {
			((SimpleRuntimeEnvironment)environment).addAsset(resource.getValue0(), resource.getValue1());
		}
		environment.getEnvironment().set(
				EnvironmentName.USE_PESSIMISTIC_LOCKING, true);
		return environment;
	}

}
