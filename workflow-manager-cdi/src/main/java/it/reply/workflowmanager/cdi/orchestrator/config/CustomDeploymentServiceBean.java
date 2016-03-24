package it.reply.workflowmanager.cdi.orchestrator.config;

import javax.enterprise.context.ApplicationScoped;

import it.reply.workflowmanager.orchestrator.config.CustomDeploymentService;

// plain CustomDeploymentService solves:
// Caused by: org.jboss.weld.exceptions.DeploymentException: WELD-001408 Unsatisfied dependencies for type [DeploymentService]
// with qualifiers [@Default] at injection point [[field] @Inject private org.jbpm.kie.services.impl.form.FormProviderServiceImpl.deploymentService]
@ApplicationScoped
/**
 * TEMPORARILY NOT USED ! (Perhaps required to boot jBPM Environment !)
 * 
 * @author l.biava
 *
 */
public class CustomDeploymentServiceBean extends CustomDeploymentService {

}
