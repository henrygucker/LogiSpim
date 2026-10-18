package org.henrygucker.logispim.fileprocessor.backend.exceptions;

/**
 * Exceptions thrown during compilation with titles to specify which process threw the exception.
 * To be displayed in a popup.
 */
public class CompilationException extends AppException {
    public CompilationException(String title, String message) {
        super(title, message);
    }
}
