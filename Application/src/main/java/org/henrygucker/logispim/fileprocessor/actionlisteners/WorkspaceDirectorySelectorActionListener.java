package org.henrygucker.logispim.fileprocessor.actionlisteners;

import org.henrygucker.logispim.fileprocessor.backend.AppConfig;
import org.henrygucker.logispim.fileprocessor.backend.WorkspaceFileHandler;
import org.henrygucker.logispim.fileprocessor.uicomponents.FileSelectorPanel;
import org.henrygucker.logispim.fileprocessor.App;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.nio.file.Path;

public class WorkspaceDirectorySelectorActionListener implements FileSelectorActionListener {
    private final App app;

    public WorkspaceDirectorySelectorActionListener() {
        this(null);
    }

    private WorkspaceDirectorySelectorActionListener(App app) {
        this.app = app;
    }

    @Override
    public FileSelectorActionListener from(App app, FileSelectorPanel panel) {
        return new WorkspaceDirectorySelectorActionListener(app);
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        Path currentPath = (Path) AppConfig.WORKSPACE.getValue();

        // Creating file chooser
        JFileChooser fileChooser = new JFileChooser(currentPath.toFile());
        fileChooser.setDialogTitle("Select New Workspace Directory");
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        int returnVal = fileChooser.showDialog(app, "Migrate Workspace Directory");
        if (returnVal != JFileChooser.APPROVE_OPTION)
            return;

        // Fetch chosen file path
        Path curr = fileChooser.getSelectedFile().toPath();

        // Finds the first existing parent directory (which is not a file) of the selected file
        while (!curr.toFile().exists() && curr.getParent() != null)
            curr = curr.getParent();

        if (!curr.toFile().isDirectory())
            curr = curr.getParent();

        // Do nothing if current workspace is selected
        if (curr.toAbsolutePath().normalize().equals(
                ((Path) AppConfig.WORKSPACE.getValue()).toAbsolutePath().normalize()
        )) {
            return;
        }

        // Updates current workspace in config and initializing the new workspace
        WorkspaceFileHandler.migrateWorkspace(curr);

        // Updates the file selectors with their new values
        this.app.refreshFileSelectors();
    }
}
