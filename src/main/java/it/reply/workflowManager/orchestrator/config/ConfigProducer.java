package it.reply.workflowManager.orchestrator.config;

import java.util.List;

import org.kie.api.io.Resource;

public interface ConfigProducer {
	
	public List<Resource> getResources();

}
