package org.henrygucker.logispim.fileprocessor.backend.exceptions;

public class AppException extends Exception {
    private final String errorMessageTitle;
    private final String errorMessage;

    public AppException(String title, String message) {
        super(title + ": " + message);

        errorMessageTitle = title;
        errorMessage = message;
    }

    public String getTitle() {
        return errorMessageTitle;
    }

    public String getIsolatedMessage() {
        return errorMessage;
    }
}
