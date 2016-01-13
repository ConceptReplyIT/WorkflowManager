package it.reply.workflowManager.orchestrator.interceptors;

import it.reply.workflowManager.orchestrator.annotations.ManageEntities;
import it.reply.workflowManager.orchestrator.bpm.ejbcommands.BaseCommand;
import it.reply.workflowManager.utils.EntityRefresher;

import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import org.kie.api.executor.CommandContext;

/**
 * Applies to the command's parameters the {@link EntityRefresher}'s method corresponding
 * to the policy specified in the {@link ManageEntities} annotation
 * 
 * @author a.brigandi
 * @see EntityRefresher
 */
@ManageEntities
@Interceptor
public class ManageEntitiesInterceptor {

	@Inject
	EntityRefresher entityRefresher;
	
	public ManageEntitiesInterceptor() {
	}

	@AroundInvoke
	public Object aroundInvoke(InvocationContext ic) throws Exception {
	//	if (ic.getMethod().getName().equals("customExecute")) {
			ManageEntities annotation = ic.getMethod().getAnnotation(ManageEntities.class);
			if (annotation == null) {
					return ic.proceed();
			}
			Class<? extends Object>[] classes = annotation.EntityClasses();
			if (ic.getParameters() == null || ic.getParameters().length != 1 || !(ic.getParameters()[0] instanceof CommandContext)) {
				return ic.proceed();
			}
			CommandContext ctx = (CommandContext) ic.getParameters()[0];
			if (ctx != null && BaseCommand.getWorkItem(ctx) != null) {
				Map<String, Object> parameters = BaseCommand.getWorkItem(ctx).getParameters();
				if (parameters != null) {
					for (Entry<String, Object> entry : parameters.entrySet()) {
						switch (annotation.policy()) {
						case DO_NOTHING:
							break;
						case MERGE:
							parameters.put(entry.getKey(), entityRefresher.mergeEntities(entry.getValue(), classes));
							break;
						case PERSIST:
							parameters.put(entry.getKey(), entityRefresher.persistEntities(entry.getValue(), classes));
							break;
						case MERGE_AND_PERSIST:
							parameters.put(entry.getKey(), entityRefresher.mergeAndPersistEntities(entry.getValue(), classes));
							break;
						case FIND:
							parameters.put(entry.getKey(), entityRefresher.findEntities(entry.getValue(), classes));
							break;
						default:
							break;
						}
					}
				}
			}
			return ic.proceed();
//		} else {
//			return ic.proceed();
//		}
	}

}
