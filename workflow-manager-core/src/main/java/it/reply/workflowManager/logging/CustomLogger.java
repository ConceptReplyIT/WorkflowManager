package it.reply.workflowManager.logging;

import org.slf4j.Logger;

public abstract class CustomLogger {

  protected Logger logger;
  protected String tag;

  public CustomLogger(Logger logger, Class<?> clazz) {
    this.logger = logger;
    setTag(clazz.toString());
  }

  public String getTag() {
    return tag;
  }

  public void setTag(String tag) {
    this.tag = "(" + tag + ") - ";
  }

  // public abstract void fatal(String message);
  //
  // public abstract void fatal(Object message);

  public abstract void error(String message);

  public abstract void error(String message, Throwable cause);

  public abstract void error(Object message);

  public abstract void info(String message);

  public abstract void info(Object message);

  public abstract void warn(String message);

  public abstract void warn(Object message);

}
