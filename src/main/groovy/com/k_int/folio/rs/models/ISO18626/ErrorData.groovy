package com.k_int.folio.rs.models.ISO18626

public class ErrorData {

	/** The type off error */
	ErrorType errorType;

	/** The value associated with this error */
	String errorValue;

	public ErrorData() {
	}

	public ErrorData(ErrorType errorType, String errorValue) {
		this.errorType = errorType;
		this.errorValue = errorValue;
	}
}
