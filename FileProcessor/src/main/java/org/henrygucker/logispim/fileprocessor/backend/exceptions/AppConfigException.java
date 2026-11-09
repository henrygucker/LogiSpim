package org.henrygucker.logispim.fileprocessor.backend.exceptions;

import org.henrygucker.logispim.fileprocessor.backend.AppConfig;

/**
 * Exception thrown by {@link AppConfig} to be displayed in a popup
 */
public class AppConfigException extends AppException {
    public AppConfigException(String message) {
        super("Config Error", message);
    }
}
