package it.reply.workflowmanager.logging;

import com.google.common.base.Preconditions;

import org.slf4j.Logger;

public abstract class CustomLogger {

  protected Logger logger;
  protected String tag;

  protected CustomLogger(Logger logger) {
    this.logger = Preconditions.checkNotNull(logger);
  }

  public String getTag() {
    return tag;
  }

  public void setTag(String tag) {
    if (tag == null) {
      this.tag = "";
    } else {
      this.tag = String.format("(%s) - ", tag);
    }
  }

  public abstract boolean isTraceEnabled();
  
  public abstract void trace(String message, Object... arguments);

  public abstract void trace(Object obj);

  public abstract boolean isDebugEnabled();
  
  public abstract void debug(String message, Object... arguments);

  public abstract void debug(Object obj);

  public abstract boolean isInfoEnabled();
  
  public abstract void info(String message, Object... arguments);

  public abstract void info(Object obj);

  public abstract boolean isWarnEnabled();
  
  public abstract void warn(String message, Object... arguments);

  public abstract void warn(Object obj);

  public abstract boolean isErrorEnabled();
  
  public abstract void error(String message, Object... arguments);

  public abstract void error(Object obj);
}
