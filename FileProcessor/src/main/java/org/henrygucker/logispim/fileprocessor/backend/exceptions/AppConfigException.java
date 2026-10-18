package org.henrygucker.logispim.fileprocessor.backend.exceptions;

/**
 * Exception thrown by {@link AppConfig} to be displayed in a popup
 */
public class AppConfigException extends AppException {
    public AppConfigException(String message) {
        super("Config Error", message);
    }
}
