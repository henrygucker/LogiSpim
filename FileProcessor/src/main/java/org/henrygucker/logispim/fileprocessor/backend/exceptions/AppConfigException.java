package org.henrygucker.logispim.fileprocessor.backend.exceptions;

public class AppConfigException extends AppException {
    public AppConfigException(String message) {
        super("Settings Error", message);
    }
}
