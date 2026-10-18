package org.henrygucker.logispim.fileprocessor.backend.exceptions;

/**
 * Base class for exceptions that are to be displayed as popups by {@link App}
 */
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
