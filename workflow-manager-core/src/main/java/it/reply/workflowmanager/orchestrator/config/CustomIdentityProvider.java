package it.reply.workflowmanager.orchestrator.config;

import java.util.Collections;
import java.util.List;

import org.kie.internal.identity.IdentityProvider;

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
