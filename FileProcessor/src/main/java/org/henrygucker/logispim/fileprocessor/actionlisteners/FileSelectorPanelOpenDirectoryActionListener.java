package org.henrygucker.logispim.fileprocessor.actionlisteners;

import org.henrygucker.logispim.fileprocessor.backend.AppConfig;
import org.henrygucker.logispim.fileprocessor.backend.WorkspaceFileHandler;
import org.henrygucker.logispim.fileprocessor.uicomponents.FileSelectorPanel;
import org.henrygucker.logispim.fileprocessor.App;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class FileSelectorPanelOpenDirectoryActionListener implements ActionListener {
    private final App app;
    private final FileSelectorPanel.FileSelectorPanelType type;

    public FileSelectorPanelOpenDirectoryActionListener(App app, FileSelectorPanel.FileSelectorPanelType type) {
        this.app = app;
        this.type = type;
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        Path currentPath = (Path) type.getAssociatedSetting().getValue();

        File currentFile = currentPath.toFile();
        while (currentFile != null && !currentFile.isDirectory())
            currentFile = currentFile.getParentFile();

        openDirectory(currentFile.toPath());
    }

    /**
     * Opens an OS-native file explorer window to the entered directory.
     * @param path A {@link Path} to the desired directory to be opened
     */
    private void openDirectory(Path path) {
        // Ensure the Desktop API is supported by the current OS platform
        if (!Desktop.isDesktopSupported()) {
            app.errorMessage("Error Opening Directory", "Desktop API is not supported on this platform.");
            return;
        }

        Desktop desktop = Desktop.getDesktop();
        File dirToOpen = new File(path.toUri());

        try {
            if (dirToOpen.exists() && dirToOpen.isDirectory()) {
                // Launches the native OS file explorer
                desktop.open(dirToOpen);
            } else {
                app.errorMessage("Directory Not Found", "The directory does not exist.");
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
            app.errorMessage("Error", "Failed to open file explorer: " + e.getMessage());
            return;
        }
    }
}
