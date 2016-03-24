package it.reply.workflowmanager.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceUnitUtil;

import org.hibernate.proxy.HibernateProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.primitives.Primitives;

public abstract class AbstractEntityRefresher implements EntityRefresher {

  protected static Logger LOG = LoggerFactory.getLogger(AbstractEntityRefresher.class);

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

  private Object manageEntities(Object o, Map<Object, Object> parsedObjects, Set<Object> entities,
      ManagementAction managementAction, Class<? extends Object>[] classes) {
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
      Class<?> entityClass = getEntityClass(oldEntity);
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
        newEntity = managementAction.manage(oldEntity);
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
        ((Object[]) o)[i] = manageEntities(((Object[]) o)[i], parsedObjects, entities,
            managementAction, classes);
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
            field.set(o,
                manageEntities(field.get(o), parsedObjects, entities, managementAction, classes));
          } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new RuntimeException(
                "Error trying to understand if " + o.toString() + " is an entity.", e);
          }
        }
      }
    } while (varClass != Object.class && (varClass = varClass.getSuperclass()) != Object.class);
    return o;
  }

  private Class<?> getEntityClass(Object entity) {
    Class<?> entityClass = null;
    if (entity instanceof HibernateProxy) {
      entityClass = ((HibernateProxy) entity).getHibernateLazyInitializer().getPersistentClass();
    } else {
      entityClass = entity.getClass();
    }
    return entityClass;
  }

  private interface ManagementAction {
    public Object manage(Object oldEntity);
  }

  @Override
  public Object findEntities(Object object, Class<? extends Object>[] classes) {
    ManagementAction find = new ManagementAction() {
      @Override
      public Object manage(Object oldEntity) {
        Object id = getPersistenceUnitUtil().getIdentifier(oldEntity);
        Object newEntity = null;
        if (id != null) {
          newEntity = getEntityManager().find(getEntityClass(oldEntity), id);
        }
        return newEntity;
      }
    };
    return manageEntities(object, null, null, find, classes);
  }

  @Override
  public Object mergeEntities(Object object, Class<? extends Object>[] classes) {
    ManagementAction merge = new ManagementAction() {
      @Override
      public Object manage(Object oldEntity) {
        Object id = getPersistenceUnitUtil().getIdentifier(oldEntity);
        Object newEntity = null;
        if (id != null) {
          newEntity = getEntityManager().merge(oldEntity);
        }
        return newEntity;
      }
    };
    return manageEntities(object, null, null, merge, classes);
  }

  @Override
  public Object persistEntities(Object object, Class<? extends Object>[] classes) {
    ManagementAction persist = new ManagementAction() {
      @Override
      public Object manage(Object oldEntity) {
        Object id = getPersistenceUnitUtil().getIdentifier(oldEntity);
        Object newEntity = null;
        if (id == null) {
          getEntityManager().persist(oldEntity);
          newEntity = oldEntity;
        }
        return newEntity;
      }
    };
    return manageEntities(object, null, null, persist, classes);
  }

  @Override
  public Object mergeAndPersistEntities(Object object, Class<? extends Object>[] classes) {
    ManagementAction mergeAndPersist = new ManagementAction() {
      @Override
      public Object manage(Object oldEntity) {
        Object id = getPersistenceUnitUtil().getIdentifier(oldEntity);
        Object newEntity = null;
        if (id != null) {
          newEntity = getEntityManager().merge(oldEntity);
        } else {
          getEntityManager().persist(oldEntity);
          newEntity = oldEntity;
        }
        return newEntity;
      }
    };
    return manageEntities(object, null, null, mergeAndPersist, classes);
  }

  @Override
  public Set<Object> getEntities(final Object object) {
    ManagementAction doNothing = new ManagementAction() {
      @Override
      public Object manage(Object oldEntity) {
        return null;
      }
    };
    Set<Object> entities = new HashSet<Object>();
    manageEntities(object, null, entities, doNothing, null);
    return entities;
  }

  @Override
  public boolean containsEntities(final Object object) {
    Set<Object> entities = getEntities(object);
    return !entities.isEmpty();
  }
}