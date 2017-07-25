package it.reply.workflowmanager.logging;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;

import java.io.IOException;
import java.io.StringWriter;

public class CustomLoggerImpl extends CustomLogger {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  protected CustomLoggerImpl(Logger logger) {
    super(logger);
  }

  @Override
  public boolean isTraceEnabled() {
    return logger.isTraceEnabled();
  }

  @Override
  public void trace(String message, Object... arguments) {
    if (isTraceEnabled()) {
      logger.trace(String.format("%s%s", tag, message), arguments);
    }
  }

  @Override
  public void trace(Object obj) {
    if (logger.isTraceEnabled()) {
      logger.trace(serializeJson(tag, obj));
    }
  }

  @Override
  public boolean isDebugEnabled() {
    return logger.isDebugEnabled();
  }

  @Override
  public void debug(String message, Object... arguments) {
    if (isDebugEnabled()) {
      logger.debug(String.format("%s%s", tag, message), arguments);
    }
  }

  @Override
  public void debug(Object obj) {
    if (logger.isDebugEnabled()) {
      logger.debug(serializeJson(tag, obj));
    }
  }

  @Override
  public boolean isInfoEnabled() {
    return logger.isInfoEnabled();
  }

  @Override
  public void info(String message, Object... arguments) {
    if (isInfoEnabled()) {
      logger.info(String.format("%s%s", tag, message), arguments);
    }
  }

  @Override
  public void info(Object obj) {
    if (logger.isInfoEnabled()) {
      logger.info(serializeJson(tag, obj));
    }
  }

  @Override
  public boolean isWarnEnabled() {
    return logger.isWarnEnabled();
  }

  @Override
  public void warn(String message, Object... arguments) {
    if (isWarnEnabled()) {
      logger.warn(String.format("%s%s", tag, message), arguments);
    }
  }

  @Override
  public void warn(Object obj) {
    if (logger.isWarnEnabled()) {
      logger.warn(serializeJson(tag, obj));
    }
  }

  @Override
  public boolean isErrorEnabled() {
    return logger.isErrorEnabled();
  }

  @Override
  public void error(String message, Object... arguments) {
    if (isErrorEnabled()) {
      logger.error(String.format("%s%s", tag, message), arguments);
    }
  }

  @Override
  public void error(Object obj) {
    if (logger.isErrorEnabled()) {
      logger.error(serializeJson(tag, obj));
    }
  }

  private String serializeJson(String tag, Object obj) {
    String result;
    try {
      if (tag == null) {
        if (obj instanceof String) {
          result = (String) obj;
        } else {
          result = OBJECT_MAPPER.writeValueAsString(obj);
        }
      } else {
        StringWriter sw = new StringWriter();
        sw.append(tag);
        if (obj instanceof String) {
          sw.append((String) obj);
        } else {
          OBJECT_MAPPER.writeValue(sw, obj);
        }
        result = sw.toString();
      }
    } catch (IOException ex) {
      logger.error("Error serializing to JSON", ex);
      result = tag;
    }
    return result != null ? result : "";
  }

}
