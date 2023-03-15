package application;

public class InvalidDescriptionException extends Exception {
	String errorMessage;
	
	public InvalidDescriptionException(String errMessage) {
        errorMessage = errMessage;
    }
	
	public String getErrorMessage() {
		return errorMessage;
	}
}
