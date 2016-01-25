package it.reply.workflowManager.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceUnitUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.proxy.HibernateProxy;

import com.google.common.primitives.Primitives;

public abstract class AbstractEntityRefresher implements EntityRefresher {

  protected static Logger LOG = LogManager.getLogger(AbstractEntityRefresher.class);

  protected abstract EntityManager getEntityManager();

  private PersistenceUnitUtil getPersistenceUnitUtil() {
    return getEntityManager().getEntityManagerFactory().getPersistenceUnitUtil();
  }

  private boolean isEntity(Object o) {
    if (isPrimitiveOrWrapper(o)) {
      return false;
    }
    try {
      getPersistenceUnitUtil().getIdentifier(o);
      return true;
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

  private boolean isPrimitiveOrWrapper(Class<?> _class) {
    return _class.isPrimitive() || Primitives.isWrapperType(_class);
  }

  private boolean isPrimitiveOrWrapper(Object o) {
    return isPrimitiveOrWrapper(o.getClass());
  }

  public enum Operation {
    NOTHING, FIND, MERGE, PERSIST, MERGE_AND_PERSIST
  }

  private Object manageEntities(Object o, Map<Object, Object> parsedObjects, Set<Object> entities,
      Operation operation, Class<? extends Object>[] classes) {
    if (o == null) {
      return o;
    }
    if (parsedObjects == null) {
      parsedObjects = new HashMap<Object, Object>();
    } else {
      if (parsedObjects.containsKey(o)) {
        Object entity = parsedObjects.get(o);
        return (entity != null ? entity : o);
      }
    }
    if (isPrimitiveOrWrapper(o)) {
      parsedObjects.put(o, null);
      return o;
    }
    if (o.getClass().isEnum()) {
      parsedObjects.put(o, null);
      return o;
    }
    if (isEntity(o)) {
      if (entities == null) {
        entities = new HashSet<Object>();
      }
      Object oldEntity = o;
      Object newEntity = null;
      Class<?> entityClass = null;
      if (oldEntity instanceof HibernateProxy) {
        entityClass = ((HibernateProxy) oldEntity).getHibernateLazyInitializer()
            .getPersistentClass();
      } else {
        entityClass = oldEntity.getClass();
      }
      boolean manage = true;
      if (classes != null && classes.length != 0) {
        manage = false;
        for (Class<? extends Object> _class : classes) {
          if (_class.isAssignableFrom(entityClass)) {
            manage = true;
            break;
          }
        }
      }
      if (manage) {
        Object id = getPersistenceUnitUtil().getIdentifier(oldEntity);
        switch (operation) {
          case FIND:
            if (id != null) {
              newEntity = getEntityManager().find(entityClass, id);
            }
            break;
          case MERGE:
            if (id != null) {
              newEntity = getEntityManager().merge(oldEntity);
            }
            break;
          case PERSIST:
            if (id == null) {
              getEntityManager().persist(oldEntity);
              newEntity = oldEntity;
            }
            break;
          case MERGE_AND_PERSIST:
            if (id != null) {
              newEntity = getEntityManager().merge(oldEntity);
            } else {
              getEntityManager().persist(oldEntity);
              newEntity = oldEntity;
            }
            break;
          case NOTHING:
          default:
            break;
        }
      }
      if (newEntity != null) {
        parsedObjects.put(oldEntity, newEntity);
        entities.add(newEntity);
        return newEntity;
      } else {
        parsedObjects.put(oldEntity, oldEntity);
        entities.add(oldEntity);
        return oldEntity;
      }
    }
    parsedObjects.put(o, null);
    if (o.getClass().isArray()) {
      Class<?> innerClass = o.getClass().getComponentType();
      if (isPrimitiveOrWrapper(innerClass)) {
        return o;
      }
      for (int i = 0; i < ((Object[]) o).length; ++i) {
        ((Object[]) o)[i] = manageEntities(((Object[]) o)[i], parsedObjects, entities, operation,
            classes);
      }
      return o;
    }
    Class<? extends Object> varClass = o.getClass();
    do {
      for (Field field : varClass.getDeclaredFields()) {
        int mod = field.getModifiers();
        if (!Modifier.isStatic(mod)) {
          if (!field.isAccessible()) {
            field.setAccessible(true);
          }
          try {
            field.set(o, manageEntities(field.get(o), parsedObjects, entities, operation, classes));
          } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new RuntimeException(
                "Error trying to understand if " + o.toString() + " is an entity.", e);
          }
        }
      }
    } while (varClass != Object.class && (varClass = varClass.getSuperclass()) != Object.class);
    return o;
  }

  @Override
  public Object findEntities(Object object, Class<? extends Object>[] classes) {
    return manageEntities(object, null, null, Operation.FIND, classes);
  }

  @Override
  public Object mergeEntities(Object object, Class<? extends Object>[] classes) {
    return manageEntities(object, null, null, Operation.MERGE, classes);
  }

  @Override
  public Object persistEntities(Object object, Class<? extends Object>[] classes) {
    return manageEntities(object, null, null, Operation.PERSIST, classes);
  }

  @Override
  public Object mergeAndPersistEntities(Object object, Class<? extends Object>[] classes) {
    return manageEntities(object, null, null, Operation.MERGE_AND_PERSIST, classes);
  }

  @Override
  public Set<Object> getEntities(final Object object) {
    Set<Object> entities = new HashSet<Object>();
    manageEntities(object, null, entities, Operation.NOTHING, null);
    return entities;
  }

  @Override
  public boolean containsEntities(final Object object) {
    Set<Object> entities = getEntities(object);
    return !entities.isEmpty();
  }
}