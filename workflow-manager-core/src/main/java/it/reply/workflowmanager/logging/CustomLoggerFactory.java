package it.reply.workflowmanager.logging;

import org.slf4j.LoggerFactory;

public class CustomLoggerFactory {
  public static <ClassType> CustomLogger getLogger(Class<ClassType> clazz) {
    return new CustomLoggerImpl(LoggerFactory.getLogger(clazz), clazz);
  }
}
