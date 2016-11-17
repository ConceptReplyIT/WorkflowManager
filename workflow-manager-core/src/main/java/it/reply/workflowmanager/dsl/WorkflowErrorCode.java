package it.reply.workflowmanager.dsl;

/**
 * BizLib error codes.
 * 
 * @author l.biava
 * 
 */
public enum WorkflowErrorCode implements ErrorCode {

    // @formatter:off
    ORC_GENERIC_ERROR(950, "A generic error occurred"),
    ORC_CONFLICTING_CONCURRENT_OPERATIONS(908,"Concurrent operations in conflict", 409),
	ORC_PERSISTENCE_ERROR(909, "Persistence error");

    // @formatter:on

    private final int code;
    private final String description;
    private final int httpStatusCode;

    private WorkflowErrorCode(int code, String description) {
	this.code = code;
	this.description = description;
	httpStatusCode = 500;
    }

    private WorkflowErrorCode(int code, String description, int httpStatusCode) {

	this.code = code;
	this.description = description;
	this.httpStatusCode = httpStatusCode;
    }

    @Override
    public String getDescription() {
	return description;
    }

    @Override
    public int getCode() {
	return code;
    }

    @Override
    public int getHttpStatusCode() {
	return httpStatusCode;
    }

    @Override
    public String getName() {
	return name();
    }

    @Override
    public String toString() {
	return code + ": " + description;
    }

    @Override
    public WorkflowErrorCode lookupFromCode(int errorCode) {
	for (WorkflowErrorCode e : values()) {
	    if (e.code == errorCode) {
		return e;
	    }
	}
	return null;
    }

    @Override
    public WorkflowErrorCode lookupFromName(String errorName) {
	for (WorkflowErrorCode e : values()) {
	    if (errorName.equals(e.name())) {
		return e;
	    }
	}
	return null;
    }
}