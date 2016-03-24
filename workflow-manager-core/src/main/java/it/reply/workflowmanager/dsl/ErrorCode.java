package it.reply.workflowmanager.dsl;

/**
 * This interface is used to specify ErrorCodes and related Error Name and
 * Description with an {@link Enum}.
 * 
 * @author l.biava
 * 
 */
public interface ErrorCode {

	/**
	 * 
	 * @return the description of the error.
	 */
	public String getDescription();

	/**
	 * 
	 * @return the code of the error.
	 */
	public int getCode();

	/**
	 * 
	 * @return the name of the error.
	 */
	public String getName();

	/**
	 * 
	 * @return the HttpStatus code of the error.
	 */
	public int getHttpStatusCode();
	
	/**
	 * 
	 * @param errorCode
	 * @return the ErrorCode Enum value which has the given code.
	 */
	public ErrorCode lookupFromCode(int errorCode);

	/**
	 * 
	 * @param errorName
	 * @return the ErrorCode Enum value which has the given name.
	 */
	public ErrorCode lookupFromName(String errorName);
}