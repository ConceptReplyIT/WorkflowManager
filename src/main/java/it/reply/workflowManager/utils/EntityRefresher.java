package it.reply.workflowManager.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnitUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.proxy.HibernateProxy;

import com.google.common.primitives.Primitives;

@Stateless
public class EntityRefresher {
	
	public static Logger LOG = LogManager.getLogger(EntityRefresher.class);
	
	@PersistenceContext(unitName = "PrismaDAL")
	private EntityManager entityManager;

	public EntityRefresher() {
	}

	private PersistenceUnitUtil getPersistenceUnitUtil() {
		return entityManager.getEntityManagerFactory().getPersistenceUnitUtil();
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
		return 	_class.isPrimitive() || Primitives.isWrapperType(_class);
	}

	private boolean isPrimitiveOrWrapper(Object o) {
		return 	isPrimitiveOrWrapper(o.getClass());
	}

	public enum Operation {
		NOTHING, FIND, MERGE, PERSIST, MERGE_AND_PERSIST
	}
		
	private Object manageEntities(Object o, Map<Object, Object> parsedObjects, Set<Object> entities, Operation operation,
			Class<? extends Object>[] classes) {
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
				entityClass = ((HibernateProxy)oldEntity).getHibernateLazyInitializer().getPersistentClass();
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
						newEntity = entityManager.find(entityClass, id);
					}
					break;
				case MERGE:
					if (id != null) {
						newEntity = entityManager.merge(oldEntity);
					}
					break;
				case PERSIST:
					if (id == null) {
						entityManager.persist(oldEntity);
						newEntity = oldEntity;
					}
					break;
				case MERGE_AND_PERSIST:
					if (id != null) {
						newEntity = entityManager.merge(oldEntity);
					} else {
						entityManager.persist(oldEntity);
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
			for (int i = 0; i < ((Object[]) o).length; ++i ) {
				((Object[]) o)[i] = manageEntities(((Object[]) o)[i], parsedObjects, entities, operation, classes);
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
						throw new RuntimeException("Error trying to understand if " + o.toString() + " is an entity.", e);
					}
	            }
	        }
	    } while (varClass != Object.class && (varClass = varClass.getSuperclass()) != Object.class);
		return o;
	}
	
	/**
	 * Finds all the detached entities contained in object and reloads them from the DB
	 * @param object The object into which find the entities.
	 * @param classes Array of {@link Class}, containing the top-level 
	 * Entities classes to which limit the Entity research.
	 * @return the input object but with entities reloaded.
	 */
	public Object findEntities(Object object, Class<? extends Object>[] classes) {
		return manageEntities(object, null, null, Operation.FIND, classes);			
	}
	
	/**
	 * Finds all the detached entities contained in object and merges them
	 * @param object The object into which find the entities.
	 * @param classes Array of {@link Class}, containing the top-level 
	 * Entities classes to which limit the Entity research.
	 * @return the input object but with the entities merged.
	 */
	public Object mergeEntities(Object object, Class<? extends Object>[] classes) {
		return manageEntities(object, null, null, Operation.MERGE, classes);			
	}
	
	/**
	 * Finds all the not yet persisted entities contained in object and persists them
	 * @param object The object into which find the entities.
	 * @param classes Array of {@link Class}, containing the top-level 
	 * Entities classes to which limit the Entity research.
	 * @return the input object but with the entities persisted.
	 */
	public Object persistEntities(Object object, Class<? extends Object>[] classes) {
		return manageEntities(object, null, null, Operation.PERSIST, classes);			
	}
	
	/**
	 * Finds all the entities contained in object, merges the detached entities and persists the new ones
	 * @param object The object into which find the entities.
	 * @param classes Array of {@link Class}, containing the top-level 
	 * Entities classes to which limit the Entity research.
	 * @return the input object but with the entities merged or persisted.
	 */
	public Object mergeAndPersistEntities(Object object, Class<? extends Object>[] classes) {
		return manageEntities(object, null, null, Operation.MERGE_AND_PERSIST, classes);			
	}
	
	/**
	 * Finds all the entities contained in object
	 * @param object The object into which find the entities.
	 * @return the {@link Set} of entities contained in object.
	 */
	public Set<Object> getEntities(final Object object) {
		Set<Object> entities = new HashSet<Object>();
		manageEntities(object, null, entities, Operation.NOTHING, null);
		return entities;
	}
	
	/**
	 * Tells if object contains entities
	 * @param object The object into which find the entities.
	 * 
	 * @return <b>true</b> if object contains entities, <b>false</b> otherwise
	 */
	public boolean containsEntities(final Object object) {
		Set<Object> entities = getEntities(object);
		return !entities.isEmpty();
	}
}