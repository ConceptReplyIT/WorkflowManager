package it.reply.workflowManager.utils;

import java.util.Set;

public interface EntityRefresher {
	
	/**
	 * Finds all the detached entities contained in object and reloads them from the DB
	 * @param object The object into which find the entities.
	 * @param classes Array of {@link Class}, containing the top-level 
	 * Entities classes to which limit the Entity research.
	 * @return the input object but with entities reloaded.
	 */
	public Object findEntities(Object object, Class<? extends Object>[] classes);
	
	/**
	 * Finds all the detached entities contained in object and merges them
	 * @param object The object into which find the entities.
	 * @param classes Array of {@link Class}, containing the top-level 
	 * Entities classes to which limit the Entity research.
	 * @return the input object but with the entities merged.
	 */
	public Object mergeEntities(Object object, Class<? extends Object>[] classes);
	
	/**
	 * Finds all the not yet persisted entities contained in object and persists them
	 * @param object The object into which find the entities.
	 * @param classes Array of {@link Class}, containing the top-level 
	 * Entities classes to which limit the Entity research.
	 * @return the input object but with the entities persisted.
	 */
	public Object persistEntities(Object object, Class<? extends Object>[] classes);
	
	/**
	 * Finds all the entities contained in object, merges the detached entities and persists the new ones
	 * @param object The object into which find the entities.
	 * @param classes Array of {@link Class}, containing the top-level 
	 * Entities classes to which limit the Entity research.
	 * @return the input object but with the entities merged or persisted.
	 */
	public Object mergeAndPersistEntities(Object object, Class<? extends Object>[] classes);
	
	/**
	 * Finds all the entities contained in object
	 * @param object The object into which find the entities.
	 * @return the {@link Set} of entities contained in object.
	 */
	public Set<Object> getEntities(final Object object);
	
	/**
	 * Tells if object contains entities
	 * @param object The object into which find the entities.
	 * 
	 * @return <b>true</b> if object contains entities, <b>false</b> otherwise
	 */
	public boolean containsEntities(final Object object);
}