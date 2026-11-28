package org.henrygucker.logispim.application.backend.exceptions;

import org.henrygucker.logispim.application.backend.AppConfig;

/**
 * Exception thrown by {@link AppConfig} to be displayed in a popup
 */
public class AppConfigException extends AppException {
    public AppConfigException(String message) {
        super("Config Error", message);
    }
}
