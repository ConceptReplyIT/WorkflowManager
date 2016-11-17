package it.reply.workflowmanager.orchestrator.bpm.ejbcommands;

import it.reply.workflowmanager.dsl.Error;
import it.reply.workflowmanager.orchestrator.bpm.WIHs.EJBWorkItemHelper;
import it.reply.workflowmanager.orchestrator.bpm.WIHs.misc.SignalEvent;
import it.reply.workflowmanager.utils.Constants;
import it.reply.workflowmanager.dsl.ErrorCode;
import it.reply.workflowmanager.dsl.WorkflowErrorCode;
import it.reply.workflowmanager.logging.CustomLogger;
import it.reply.workflowmanager.logging.CustomLoggerFactory;

import javax.annotation.Resource;
import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceException;
import javax.transaction.Status;
import javax.transaction.UserTransaction;

import org.hibernate.StaleObjectStateException;
import org.kie.api.runtime.process.WorkItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

import org.kie.api.executor.CommandContext;
import org.kie.api.executor.ExecutionResults;

/**
 * Abstract base class for command implementation.<br/>
 * This sets up logger and some variables from the Executor command context. It also manages logging
 * at the start and end of command and provides helper methods for error and result handling.
 * 
 * @author l.biava
 * 
 */
public abstract class AbstractBaseCommand implements IEJBCommand {

  private static final Logger LOG = LoggerFactory.getLogger(AbstractBaseCommand.class);

  protected CustomLogger logger;

  @Resource
  private UserTransaction userTx;

  public AbstractBaseCommand() {
    logger = CustomLoggerFactory.getLogger(this.getClass());
  }

  /**
   * Must implement custom execution logic.
   * 
   * @param ctx
   *          - contextual data given by the executor service
   * @return returns any results in case of successful execution
   * @throws Exception
   *           in case execution failed and shall be retried if possible
   */
  protected abstract ExecutionResults customExecute(CommandContext ctx) throws Exception;

  /**
   * Returns the proxy on which call business methods.
   */
  protected abstract AbstractBaseCommand getFacade();

  public static WorkItem getWorkItem(CommandContext ctx) {
    return (WorkItem) ctx.getData(Constants.WORKITEM);
  }

  @SuppressWarnings("unchecked")
  public static <T> T getParameter(CommandContext ctx, String parameterName) {
    WorkItem wi = getWorkItem(ctx);
    Object parameter = wi.getParameter(parameterName);
    try {
      return (T) parameter;
    } catch (ClassCastException ex) {
      LOG.error(String.format("Error retrieving parameter %s in WorkItem %s", parameterName, wi.getName()), ex);
      return null;
    }
  }

  public static Long getProcessInstanceId(CommandContext ctx) {
    return EJBWorkItemHelper.getProcessInstanceId(ctx);
  }

  public static it.reply.workflowmanager.dsl.Error getErrorResult(CommandContext ctx) {
    return (it.reply.workflowmanager.dsl.Error) getWorkItem(ctx)
        .getParameter(Constants.ERROR_RESULT);
  }

  /**
   * <b>This method SHOULD NOT be overridden !</b> <br/>
   * Use the {@link BaseCommand#customExecute(CommandContext)} method for the command logic.
   */
  public ExecutionResults execute(CommandContext ctx) throws Exception {

    // If the command is not an EJB an IllegalStateException will be thrown
    AbstractBaseCommand proxyCommand = getFacade();

    logCommandStarted(ctx);

    ExecutionResults exRes = new ExecutionResults();
    int maxNumOfTries = 1;
    if (this instanceof RetriableCommand) {
      maxNumOfTries = ((RetriableCommand) this).getNumOfMaxRetries(ctx);
      if (maxNumOfTries <= 0) {
        throw new IllegalArgumentException("Max num of retries must be > 0");
      }
    }

    if (userTx.getStatus() == Status.STATUS_ACTIVE) {
      throw new IllegalStateException("There must be no active transaction");
    }

    int tries = 0;
    boolean retryError;
    do {
      retryError = false;
      userTx.begin();
      try {
        if (tries == 0) {
          exRes = proxyCommand.customExecute(ctx);
        } else {
          logCommandRetry(exRes, tries, maxNumOfTries);
          exRes = ((RetriableCommand) proxyCommand).retry(ctx);
        }
        if (userTx.getStatus() == Status.STATUS_MARKED_ROLLBACK) {
          userTx.rollback();
        } else {
          userTx.commit();
        }
      } catch (Exception e) {
        userTx.rollback();
        boolean persistenceExceptionFound = false;
        Throwable cause = e;
        while (cause != null) {
          if (cause instanceof PersistenceException || cause instanceof StaleObjectStateException) {
            handlePersistenceException((Exception) cause, exRes);
            persistenceExceptionFound = true;
            break;
          } else {
            cause = !cause.equals(cause.getCause()) ? cause.getCause() : null;
          }
        }
        if (!persistenceExceptionFound) {
          throw e;
        }
      }
      SignalEvent<?> signalEvent = (SignalEvent<?>) exRes.getData(Constants.SIGNAL_EVENT);
      if (signalEvent != null && signalEvent.getType() == SignalEvent.SignalEventType.ERROR
          && signalEvent.getSubType() == SignalEvent.SignalEventSubType.PERSISTENCE) {
        retryError = true;
      }
    } while (retryError && ++tries < maxNumOfTries);

    logCommandEnded(exRes);

    return exRes;
  }

  /**
   * Logs command started info.
   */
  protected void logCommandStarted(final CommandContext ctx) {
    String WIId = "TBD";
    String tag = "(Task: " + this.getClass().getName() + ", PID: " + getProcessInstanceId(ctx)
        + ", WIId: " + WIId + ", params: " + getWorkItem(ctx).getParameters() + ") - ";
    logger.setTag(tag);
    logger.info("STARTED");
  }

  /**
   * Logs command retry info.
   */
  protected void logCommandRetry(final ExecutionResults exResults, int tryNum, int maxNumOfTries) {
    try {
      StringBuilder sb = new StringBuilder();
      sb.append("RETRY, Try ").append(tryNum).append(" of ").append(maxNumOfTries).append(".");
      @SuppressWarnings("unchecked")
      SignalEvent<Error> signalEvent = (SignalEvent<Error>) exResults
          .getData(Constants.SIGNAL_EVENT);
      if (signalEvent != null && signalEvent.getPayload() != null) {
        sb.append("\nRetry caused by:\n").append(signalEvent.getPayload().getVerbose())
            .append("\n");
      }
      logger.info(sb.toString());
    } catch (Exception ex) {
      LOG.warn("Cannot log EJBCommand result.", ex);
    }
  }

  /**
   * Logs command ended info.
   */
  protected void logCommandEnded(final ExecutionResults exResults) {
    try {
      logger.info("ENDED, ResultStatus(" + exResults.getData(Constants.RESULT_STATUS) + "), Result("
          + (exResults.getData("Result") != null ? exResults.getData("Result") : "") + ")");
    } catch (Exception ex) {
      LOG.warn("Cannot log EJBCommand result.", ex);
    }
  }

  protected ExecutionResults handlePersistenceException(Exception ex, ExecutionResults exResults) {
    SignalEvent<Error> signalEvent = new SignalEvent<Error>(SignalEvent.SignalEventType.ERROR,
        SignalEvent.SignalEventSubType.PERSISTENCE);
    StringBuilder verbose = new StringBuilder("Unable to perform the operation");
    boolean optLock;
    if (ex instanceof OptimisticLockException) {
      optLock = true;
      verbose.append(" because of conflicting operations on entity ");
      Object entity = ((OptimisticLockException) ex).getEntity();
      if (entity != null) {
        verbose.append(entity.getClass().getCanonicalName());
      } else {
        verbose.append("UNKNOWN");
      }
    } else if (ex instanceof StaleObjectStateException) {
      optLock = true;
      verbose.append(" because of conflicting operations on entity ");
      String entityName = ((StaleObjectStateException) ex).getEntityName();
      if (!Strings.isNullOrEmpty(entityName)) {
        verbose.append(entityName);
      } else {
        verbose.append("UNKNOWN");
      }
    } else {
      optLock = false;
    }
    ErrorCode errorCode = optLock ? WorkflowErrorCode.ORC_CONFLICTING_CONCURRENT_OPERATIONS
        : WorkflowErrorCode.ORC_PERSISTENCE_ERROR;
    Error error = generateError(errorCode);
    error.setVerbose(verbose.toString());
    signalEvent.setPayload(error);
    return errorOccurred(signalEvent, exResults);
  }

  protected ExecutionResults errorOccurred(ErrorCode errorCode, String verbose,
      ExecutionResults exResults) {
    Error error = generateError(errorCode);
    error.setVerbose(verbose);
    return errorOccurred(error, exResults);
  }

  protected ExecutionResults errorOccurred(ErrorCode errorCode, Throwable t,
      ExecutionResults exResults) {
    Error error = generateError(errorCode);
    error.setVerbose(t);
    return errorOccurred(error, exResults);
  }

  protected ExecutionResults errorOccurred(Error error, ExecutionResults exResults) {
    SignalEvent<Error> signalEvent = new SignalEvent<Error>(SignalEvent.SignalEventType.ERROR,
        SignalEvent.SignalEventSubType.GENERIC);
    signalEvent.setPayload(error);
    return errorOccurred(signalEvent, exResults);
  }

  protected ExecutionResults errorOccurred(SignalEvent<Error> signalEvent,
      ExecutionResults exResults) {

    Error error = signalEvent.getPayload();
    exResults.setData(Constants.RESULT_STATUS, "ERROR");
    exResults.setData(Constants.ERROR_RESULT, error);
    exResults.setData(Constants.SIGNAL_EVENT, signalEvent);

    logger.error("ERROR: " + error);

    return exResults;
  }

  /**
   * Helper method to set the result output variable of the command.
   * 
   * @param result
   *          the result data.
   * @param exResults
   *          {@link ExecutionResults} of the command for creating the result output variable.
   */
  protected <ResultType> ExecutionResults resultOccurred(ResultType result,
      ExecutionResults exResults) {
    exResults.setData(Constants.RESULT_STATUS, "OK");
    exResults.setData(Constants.OK_RESULT, result);
    return exResults;
  }

  protected <ResultType> ExecutionResults resultOccurred(ResultType result) {
    ExecutionResults exResults = new ExecutionResults();
    exResults.setData(Constants.RESULT_STATUS, "OK");
    exResults.setData(Constants.OK_RESULT, result);
    return exResults;
  }

  protected it.reply.workflowmanager.dsl.Error generateError(ErrorCode errorCode) {
    return new it.reply.workflowmanager.dsl.Error(errorCode);
  }
}
