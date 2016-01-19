package it.reply.workflowManager.orchestrator.config;

import it.reply.workflowManager.utils.Constants;

import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import javax.transaction.TransactionManager;

import org.jbpm.runtime.manager.impl.SimpleRuntimeEnvironment;
import org.jbpm.services.cdi.impl.manager.InjectableRegisterableItemsFactory;
import org.kie.api.executor.ExecutorService;
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

	@Inject
	private ExecutorService executorService;
	
	@Inject
	private ConfigProducer configProducer;

	@PersistenceUnit(unitName = Constants.PERSISTENCE_UNIT_NAME)
	private EntityManagerFactory emf;

	@Resource(mappedName = "java:jboss/TransactionManager")
	private TransactionManager tm;

	@Produces
	@PersistenceUnit(unitName = Constants.PERSISTENCE_UNIT_NAME)
	public EntityManagerFactory produceEntityManagerFactory() {
		return emf;
	}

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
				.addEnvironmentEntry(Constants.EXECUTOR_SERVICE, executorService)
				.get();
		for (org.kie.api.io.Resource resource : configProducer.getResources()) {
			((SimpleRuntimeEnvironment)environment).addAsset(resource, resource.getResourceType());
		}
		environment.getEnvironment().set(
				EnvironmentName.USE_PESSIMISTIC_LOCKING, true);
		return environment;
	}
	
	public void disposeEnvironment(@Disposes @Singleton @PerProcessInstance @PerRequest RuntimeEnvironment re) {
		re.close();
	}

}
