package it.reply.workflowManager.spring.orchestrator.bpm.ejbcommands;

import it.reply.workflowManager.orchestrator.bpm.ejbcommands.AbstractBaseCommand;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * Abstract base class for command implementation.<br/>
 * This sets up logger and some variables from the Executor command context. It also manages logging
 * at the start and end of command and provides helper methods for error and result handling.
 * 
 * @author l.biava
 * 
 */
// @ManageEntities
public abstract class BaseCommand extends AbstractBaseCommand {

  private static final Logger LOG = LogManager.getLogger(BaseCommand.class);

}
