package org.henrygucker.logispim.fileprocessor.actionlisteners;

import org.henrygucker.logispim.fileprocessor.App;
import org.henrygucker.logispim.fileprocessor.backend.AppConfig;
import org.henrygucker.logispim.fileprocessor.backend.WorkspaceFileHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class OpenLogisimCircuitActionListener implements ActionListener {

    private final App app;

    public OpenLogisimCircuitActionListener(App app) {
        this.app = app;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Path circPath = WorkspaceFileHandler.getLogisimCircPath();

        if (circPath == null)
                app.errorMessage("Error", "Failed to locate Logisim-evolution circuit file. Try" +
                        " restarting or reinstalling the app.");

        File circFile = circPath.toFile();

        try {
            // Check if the current platform supports the Desktop API
            if (!Desktop.isDesktopSupported()) {
                app.errorMessage("Error", "Desktop API is not supported on this platform.");
                return;
            }

            Desktop desktop = Desktop.getDesktop();

            // Ensure the file actually exists before trying to open it
            if (circFile.exists()) {
                desktop.open(circFile);
            } else {
                app.errorMessage("Error", "The Logisim-evolution circuit file does not exist. Try" +
                        " reinitializing the circuit.");
            }

        } catch (IOException exception) {
            // Thrown if the associated application is missing or fails to launch
            app.errorMessage("Error", "Failed to open the Logisim-evolution circuit file." +
                    "Error: " + exception.getMessage());
        } catch (IllegalArgumentException exception) {
            // Thrown if the file parameter is null
        }
    }

}
