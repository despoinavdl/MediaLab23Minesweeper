package application;

public class InvalidValueException extends Exception {
	String errorMessage;
	
	public InvalidValueException(String errMessage) {
        errorMessage = errMessage;
    }
	
	public String getErrorMessage() {
		return errorMessage;
	}
}
