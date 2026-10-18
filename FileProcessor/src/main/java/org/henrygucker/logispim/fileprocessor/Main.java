package org.henrygucker.logispim.fileprocessor;

import org.henrygucker.logispim.fileprocessor.backend.AppConfig;
import org.henrygucker.logispim.fileprocessor.backend.WorkspaceFileHandler;
import org.henrygucker.logispim.fileprocessor.backend.exceptions.AppConfigException;

import java.io.IOException;
import java.nio.file.Path;

public class Main {
    private static final String VERSION = "1.0.0-SNAPSHOT";

    public static void main(String[] args) {
        String lastUsedVersion = (String) AppConfig.LAST_USED_LOGISPIM_VERSION.getValue();

        // Check if LogiSpim has never run before
        if (lastUsedVersion.equals("N/A")) {
            // First initialization of LogiSpim

            // Attempt to initialize workspace
            try {
                WorkspaceFileHandler.initializeWorkspace();
            } catch (IOException e) {
                System.err.println("Failed to initialize workspace at path \""
                + ((Path) AppConfig.WORKSPACE_DIRECTORY.getValue()).toAbsolutePath().normalize().toString() + "\"");
                e.printStackTrace(System.err);
                return;
            }

            // VERSION is always of type String (as is required), so no exceptions are ever thrown
            try {
                AppConfig.LAST_USED_LOGISPIM_VERSION.setValue(VERSION);
            } catch (AppConfigException e) {}

        } else if (!lastUsedVersion.equals(VERSION)) {
            // Create any necessary changes since the last used version

            // Currently none to be made
        }

        App app = new App("LogiSpim");
    }
}
