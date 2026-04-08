package org.henrygucker.logispim.logispim.fileprocessor.backend.exceptions;

public class SettingsManagerException extends AppException {
    public SettingsManagerException(String message) {
        super("Settings Error", message);
    }
}
