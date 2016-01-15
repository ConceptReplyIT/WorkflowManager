package it.reply.workflowManager.dsl;

import java.io.Serializable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.base.Throwables;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Error implements Serializable {

	private static final long serialVersionUID = -6598948917927003416L;
	
	private int errorCode;
	private String errorName;
	private String errorMsg;
	private String verbose;
	
	public Error() {
	}

	public Error(ErrorCode ec) {
		this(ec, (String) null);
	}
	
	public Error(ErrorCode ec, Throwable t) {
		this(ec, Throwables.getStackTraceAsString(t));
	}
	
	public Error(ErrorCode ec, String verbose) {
		this.errorCode = ec.getCode();
		this.errorMsg = ec.getDescription();
		this.errorName = ec.getName();
		this.verbose = verbose;
	}

	public int getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}

	public String getErrorName() {
		return errorName;
	}

	public void setErrorName(String errorName) {
		this.errorName = errorName;
	}

	public String getErrorMsg() {
		return errorMsg;
	}

	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}

	public String getVerbose() {
		return verbose;
	}

	public void setVerbose(String verbose) {
		this.verbose = verbose;
	}
	
	public void setVerbose(Throwable throwable) {
		this.verbose = Throwables.getStackTraceAsString(throwable);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public boolean equals(Object other) {
		return EqualsBuilder.reflectionEquals(this, other);
	}

}