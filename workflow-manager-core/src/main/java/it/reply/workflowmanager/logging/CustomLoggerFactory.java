package it.reply.workflowmanager.logging;

import org.slf4j.LoggerFactory;

public class CustomLoggerFactory {

  private CustomLoggerFactory() {
  }

  public static <T> CustomLogger getLogger(Class<T> clazz) {
    return new CustomLoggerImpl(LoggerFactory.getLogger(clazz));
  }
}
