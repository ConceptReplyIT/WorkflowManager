package it.reply.workflowmanager.orchestrator.config;

import java.util.Collection;

import org.jbpm.services.api.DeploymentEventListener;
import org.jbpm.services.api.DeploymentService;
import org.jbpm.services.api.ListenerSupport;
import org.jbpm.services.api.model.DeployedUnit;
import org.jbpm.services.api.model.DeploymentUnit;
import org.kie.api.runtime.manager.RuntimeManager;

/**
 * TEMPORARILY NOT USED ! (Perhaps required to boot jBPM Environment !)
 * 
 * @author l.biava
 *
 */
public class CustomDeploymentService implements DeploymentService, ListenerSupport {

  public void deploy(DeploymentUnit deploymentUnit) {
  }

  public void undeploy(DeploymentUnit deploymentUnit) {
  }

  public RuntimeManager getRuntimeManager(String s) {
    return null;
  }

  public DeployedUnit getDeployedUnit(String s) {
    return null;
  }

  public Collection<DeployedUnit> getDeployedUnits() {
    return null;
  }

  @Override
  public void activate(String deploymentId) {
    // TODO Auto-generated method stub

  }

  @Override
  public void deactivate(String deploymentId) {
    // TODO Auto-generated method stub

  }

  @Override
  public boolean isDeployed(String deploymentUnitId) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void addListener(DeploymentEventListener listener) {
    // TODO Auto-generated method stub

  }

  @Override
  public void removeListener(DeploymentEventListener listener) {
    // TODO Auto-generated method stub

  }

  @Override
  public Collection<DeploymentEventListener> getListeners() {
    // TODO Auto-generated method stub
    return null;
  }
}
