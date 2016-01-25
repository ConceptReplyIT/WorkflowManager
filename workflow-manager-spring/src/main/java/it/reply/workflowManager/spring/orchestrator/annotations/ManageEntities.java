package it.reply.workflowManager.spring.orchestrator.annotations;

/// **
// * Binds EJBCommands' methods to {@link ManageEntitiesInterceptor} which finds all the entities
// * contained inside the parameters of the intercepted method. The EntityClasses parameter accepts
/// an
// * array of {@link Class}, containing the top-level Entities classes to which limit the Entity
// * research. The {@link Policy} parameter, specifies what action should be done to the found
// * entities:
// * <ul>
// * <li>{@link Policy#DO_NOTHING}: Do nothing</li>
// * <li>{@link Policy#FIND}: Find detached entities in the DB</li>
// * <li>{@link Policy#MERGE}: Merge the detached entities, ignoring the not yet persisted ones</li>
// * <li>{@link Policy#PERSIST}: Persist the new entities</li>
// * <li>{@link Policy#MERGE_AND_PERSIST}: Merge the detached Entities and persist the new ones</li>
// * </ul>
// *
// * @author a.brigandi
// *
// */
// @Inherited
// @InterceptorBinding
// @Target({ ElementType.TYPE, ElementType.METHOD })
// @Retention(RetentionPolicy.RUNTIME)
// @Documented
// public @interface ManageEntities {
// public enum Policy {
// DO_NOTHING, FIND, MERGE, PERSIST, MERGE_AND_PERSIST
// }
//
// @Nonbinding
// Policy policy() default Policy.DO_NOTHING;
//
// @Nonbinding
// Class<? extends Object>[] EntityClasses() default {};
// }
