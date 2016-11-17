package it.reply.workflowmanager.orchestrator.config;

import org.apache.commons.io.IOUtils;
import org.jbpm.bpmn2.xml.XmlBPMNProcessDumper;
import org.kie.api.io.Resource;
import org.kie.internal.io.ResourceFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface ConfigProducer {

  public List<Resource> getJbpmResources();

  public List<WorkflowResource> getWorkflowResources();

  public int getExecutorServiceThreadPoolSize();

  public int getExecutorServiceInterval();

  public static class WorkflowResource {
    private static XmlBPMNProcessDumper processReader = XmlBPMNProcessDumper.INSTANCE;

    private final Resource resource;
    private final String processId;

    public WorkflowResource(String resourcePath) throws IOException {
      this(ResourceFactory.newClassPathResource(resourcePath));
    }

    public WorkflowResource(Resource resource) throws IOException {
      this.resource = resource;
      try (InputStream is = resource.getInputStream()) {
        String processDefinition = IOUtils.toString(is, "UTF-8");
        processId = processReader.readProcess(processDefinition).getId();
      }
    }

    public Resource getResource() {
      return resource;
    }

    public String getProcessId() {
      return processId;
    }

  }
}
