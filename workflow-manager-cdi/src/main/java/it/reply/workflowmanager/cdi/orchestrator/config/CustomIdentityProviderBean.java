package it.reply.workflowmanager.cdi.orchestrator.config;

import javax.enterprise.context.ApplicationScoped;

import it.reply.workflowmanager.orchestrator.config.CustomIdentityProvider;

// dummy CustomIdentityProvider solves:
// org.jboss.weld.exceptions.DeploymentException: WELD-001408 Unsatisfied dependencies for type [IdentityProvider]
// with qualifiers [@Default] at injection point [[field] @Inject private org.jbpm.kie.services.impl.KModuleDeploymentService.identityProvider]
@ApplicationScoped
/**
 * TEMPORARILY NOT USED ! (Perhaps required to boot jBPM Environment !)
 * 
 * @author l.biava
 *
 */
public class CustomIdentityProviderBean extends CustomIdentityProvider {

}
