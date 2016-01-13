package it.reply.workflowManager.orchestrator.config;

import java.util.Collections;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import org.kie.internal.identity.IdentityProvider;

// dummy CustomIdentityProvider solves:
// org.jboss.weld.exceptions.DeploymentException: WELD-001408 Unsatisfied dependencies for type [IdentityProvider]
// with qualifiers [@Default] at injection point [[field] @Inject private org.jbpm.kie.services.impl.KModuleDeploymentService.identityProvider]
@ApplicationScoped
/**
 * TEMPORARILY NOT USED ! (Perhaps required to boot jBPM Environment !)
 * @author l.biava
 *
 */
public class CustomIdentityProvider implements IdentityProvider {

	public String getName() {
		return "dummy";
	}

	public List<String> getRoles() {

		return Collections.emptyList();
	}

	@Override
	public boolean hasRole(String role) {
		// TODO Auto-generated method stub
		return false;
	}

}
