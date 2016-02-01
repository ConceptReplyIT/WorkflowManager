/*
 * Copyright 2010 salaboy.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * under the License.
 */
package it.reply.workflowManager.orchestrator.config;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Id;
import javax.persistence.Persistence;
import javax.persistence.PersistenceUnitUtil;

import org.drools.core.common.DroolsObjectInputStream;
import org.drools.persistence.TransactionAware;
import org.drools.persistence.TransactionManager;
import org.kie.api.marshalling.ObjectMarshallingStrategy;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.EnvironmentName;
import org.kie.internal.runtime.Cacheable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.primitives.Primitives;

@Deprecated
public class CustomJPAPlaceholderResolverStrategy
    implements ObjectMarshallingStrategy, TransactionAware, Cacheable {
  @SuppressWarnings("unused")
  private static Logger log = LoggerFactory.getLogger(CustomJPAPlaceholderResolverStrategy.class);
  private EntityManagerFactory emf;
  private PersistenceUnitUtil puu;
  private ClassLoader classLoader;

  private static final ThreadLocal<EntityManager> persister = new ThreadLocal<EntityManager>();

  public CustomJPAPlaceholderResolverStrategy(Environment env) {
    this.emf = (EntityManagerFactory) env.get(EnvironmentName.ENTITY_MANAGER_FACTORY);
    puu = this.emf.getPersistenceUnitUtil();
  }

  public CustomJPAPlaceholderResolverStrategy(EntityManagerFactory emf) {
    this.emf = emf;
    puu = this.emf.getPersistenceUnitUtil();
  }

  public CustomJPAPlaceholderResolverStrategy(String persistenceUnit, ClassLoader cl) {
    ClassLoader tccl = Thread.currentThread().getContextClassLoader();

    try {
      // override tccl so persistence unit can be found from within given class loader - e.g. kjar
      Thread.currentThread().setContextClassLoader(cl);

      this.emf = Persistence.createEntityManagerFactory(persistenceUnit);
      puu = this.emf.getPersistenceUnitUtil();

    } finally {
      Thread.currentThread().setContextClassLoader(tccl);
    }
    this.classLoader = cl;
  }

  @Override
  public boolean accept(Object object) {
    // if (isEntity(object.getClass())) {
    // return true;
    // } else if (containNonTransientEntities(object.getClass())){
    // throw new UnsupportedOperationException("Command parameters must not have inner entities.
    // Entities must only be directly contained by the map.");
    // }
    return containsEntities(object);
    // return false;
  }

  private <T> void setIdAsProperty(T entity, T fakeEntity, Method getterMethod)
      throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    String getterName = getterMethod.getName();
    String fieldName = null;
    if (getterName.startsWith("is")) {
      fieldName = getterName.substring(2);
    } else if (getterName.startsWith("get")) {
      fieldName = getterName.substring(3);
    } else {
      throw new IllegalArgumentException(getterMethod.getName() + " not a valid getter name.");
    }
    String setterName = "set" + fieldName;

    Class<?> entityClass = fakeEntity.getClass();
    Method setterMethod = null;
    do {
      try {
        setterMethod = entityClass.getDeclaredMethod(setterName, getterMethod.getReturnType());
        break;
      } catch (NoSuchMethodException e) {
        continue;
      }
    } while ((entityClass = entityClass.getSuperclass()) != null);
    if (setterMethod == null) {
      throw new RuntimeException("Couldn't find setter method.");
    }
    setterMethod.invoke(fakeEntity, getterMethod.invoke(entity));
  }

  private <T> T wrapEntity(T entity, Object id) throws InstantiationException,
      IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    boolean isEmbeddedID = false;
    boolean isFieldAccess = false;
    if (id.getClass().isAnnotationPresent(Embeddable.class)) {
      isEmbeddedID = true;
    }
    Class<?> varClass = entity.getClass();
    @SuppressWarnings("unchecked")
    T fakeEntity = (T) varClass.newInstance();

    do {
      for (Field field : varClass.getDeclaredFields()) {
        if (!field.isAccessible()) {
          field.setAccessible(true);
        }
        if (isEmbeddedID) {
          if (field.isAnnotationPresent(EmbeddedId.class)) {
            field.set(fakeEntity, field.get(entity));
            return fakeEntity;
          }
        } else {
          if (field.isAnnotationPresent(Id.class)) {
            field.set(fakeEntity, field.get(entity));
            isFieldAccess = true;
            if (id.getClass().equals(field.getType())) {
              // not composite PK
              return fakeEntity;
            }
          }
        }
      }
    } while ((varClass = varClass.getSuperclass()) != null);
    if (isFieldAccess) {
      // composite PK should be done
      return fakeEntity;
    }
    varClass = entity.getClass();
    do {
      for (Method method : varClass.getDeclaredMethods()) {
        if (!method.isAccessible()) {
          method.setAccessible(true);
        }
        if (isEmbeddedID) {
          if (method.isAnnotationPresent(EmbeddedId.class)) {
            setIdAsProperty(entity, fakeEntity, method);
            return fakeEntity;
          }
        } else {
          if (method.isAnnotationPresent(Id.class)) {
            setIdAsProperty(entity, fakeEntity, method);
            if (id.getClass().equals(method.getReturnType())) {
              // not composite PK
              return fakeEntity;
            }
          }
        }
      }
    } while ((varClass = varClass.getSuperclass()) != null);
    return fakeEntity;
  }

  private <T> T wrapEntities(T object, EntityManager em) throws InstantiationException,
      IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    if (object == null) {
      return object;
    }
    if (isEntity(object)) {
      Object id = this.puu.getIdentifier(object);
      if (id == null) {
        // not persisted entity
        em.persist(object);
      } else {
        em.merge(object);
      }
      em.flush();
      id = this.puu.getIdentifier(object);
      T fakeEntity = wrapEntity(object, id);
      assert id.equals(this.puu.getIdentifier(fakeEntity));
      return fakeEntity;
    }
    if (isPrimitiveOrWrapper(object)) {
      return object;
    }
    Class<? extends Object> varClass = object.getClass();
    if (varClass.isArray()) {
      Class<?> innerClass = varClass.getComponentType();
      if (isPrimitiveOrWrapper(innerClass)) {
        return object;
      }
      for (int i = 0; i < ((Object[]) object).length; ++i) {
        ((Object[]) object)[i] = wrapEntities(((Object[]) object)[i], em);
      }
      return object;
    }

    do {
      for (Field field : varClass.getDeclaredFields()) {
        int mod = field.getModifiers();
        if (!Modifier.isStatic(mod) && !Modifier.isTransient(mod)) {
          if (!field.isAccessible()) {
            field.setAccessible(true);
          }
          field.set(object, wrapEntities(field.get(object), em));
        }
      }
    } while ((varClass = varClass.getSuperclass()) != null);
    return object;
  }

  @SuppressWarnings("unchecked")
  private <T> T unwrapEntities(T object, EntityManager em)
      throws IllegalArgumentException, IllegalAccessException {
    if (object == null) {
      return object;
    }
    if (isEntity(object)) {
      Object id = this.puu.getIdentifier(object);
      if (id == null) {
        throw new NullPointerException("Trying to unwrap an entity with null id.");
      }
      return (T) em.find(object.getClass(), id);
    }
    if (isPrimitiveOrWrapper(object)) {
      return object;
    }
    Class<? extends Object> varClass = object.getClass();
    if (varClass.isArray()) {
      Class<?> innerClass = varClass.getComponentType();
      if (isPrimitiveOrWrapper(innerClass)) {
        return object;
      }
      for (int i = 0; i < ((Object[]) object).length; ++i) {
        ((Object[]) object)[i] = unwrapEntities(((Object[]) object)[i], em);
      }
      return object;
    }

    do {
      for (Field field : varClass.getDeclaredFields()) {
        int mod = field.getModifiers();
        if (!Modifier.isStatic(mod) && !Modifier.isTransient(mod)) {
          if (!field.isAccessible()) {
            field.setAccessible(true);
          }
          field.set(object, unwrapEntities(field.get(object), em));
        }
      }
    } while ((varClass = varClass.getSuperclass()) != null);
    return object;
  }

  @Override
  public void write(ObjectOutputStream os, Object object) throws IOException {
    try {
      wrapEntities(object, this.getEntityManager());
    } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
        | InvocationTargetException e) {
      throw new RuntimeException("Error wrapping entities of object " + object.toString(), e);
    }
    os.writeObject(object);
  }

  @Override

  public Object read(ObjectInputStream is) throws IOException, ClassNotFoundException {
    Object object = is.readObject();
    EntityManager em = getEntityManager();
    try {
      return unwrapEntities(object, em);
    } catch (IllegalAccessException | IllegalArgumentException e) {
      throw new RuntimeException("Error unwrapping entities of object " + object.toString(), e);
    }
  }

  @Override
  public byte[] marshal(Context context, ObjectOutputStream os, Object object) throws IOException {

    ByteArrayOutputStream buff = new ByteArrayOutputStream();
    ObjectOutputStream oos = new ObjectOutputStream(buff);
    write(oos, object);
    oos.close();
    return buff.toByteArray();
  }

  @Override

  public Object unmarshal(Context context, ObjectInputStream ois, byte[] object,
      ClassLoader classloader) throws IOException, ClassNotFoundException {
    ClassLoader clToUse = classloader;
    if (this.classLoader != null) {
      clToUse = this.classLoader;
    }

    DroolsObjectInputStream is = new DroolsObjectInputStream(new ByteArrayInputStream(object),
        clToUse);
    Object o = read(is);
    is.close();

    return o;
  }

  @Override

  public Context createContext() {
    // no need for context
    return null;
  }

  private boolean isEntity(Object o) {
    if (isPrimitiveOrWrapper(o)) {
      return false;
    }
    try {
      this.puu.getIdentifier(o);
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

  @Override
  public void onStart(TransactionManager txm) {
    if (persister.get() == null) {
      EntityManager em = emf.createEntityManager();
      persister.set(em);
    }
  }

  private boolean containsEntities(Object o) {
    if (o == null) {
      return false;
    }
    if (isPrimitiveOrWrapper(o)) {
      return false;
    }
    if (isEntity(o)) {
      return true;
    }
    if (o.getClass().isArray()) {
      Class<?> innerClass = o.getClass().getComponentType();
      if (isPrimitiveOrWrapper(innerClass)) {
        return false;
      }
      for (Object innerObject : (Object[]) o) {
        if (containsEntities(innerObject)) {
          return true;
        }
      }
      return false;
    }
    Class<? extends Object> varClass = o.getClass();
    do {
      for (Field field : varClass.getDeclaredFields()) {
        int mod = field.getModifiers();
        if (!Modifier.isStatic(mod) && !Modifier.isTransient(mod) && !field.isEnumConstant()) {
          if (!field.isAccessible()) {
            field.setAccessible(true);
          }
          try {
            if (containsEntities(field.get(o))) {
              return true;
            }
          } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new RuntimeException(
                "Error trying to understand if " + o.toString() + " is an entity.");
          }
        }
      }
    } while ((varClass = varClass.getSuperclass()) != null);
    return false;
  }

  @Override
  public void onEnd(TransactionManager txm) {
    EntityManager em = persister.get();
    if (em != null) {
      em.close();
      persister.set(null);
    }
  }

  protected EntityManager getEntityManager() {
    EntityManager em = persister.get();
    if (em != null) {
      return em;
    }
    return emf.createEntityManager();
  }

  @Override
  public void close() {
    this.puu = null;
    if (this.emf != null) {
      this.emf.close();
      this.emf = null;
    }
  }

}
