package org.henrygucker.logispim.logispim.fileprocessor.actionlisteners;

import org.henrygucker.logispim.logispim.fileprocessor.backend.exceptions.SettingsManagerException;
import org.henrygucker.logispim.logispim.fileprocessor.uicomponents.FileSelectorPanel;
import org.henrygucker.logispim.logispim.fileprocessor.App;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.nio.file.Path;

public class OutputDirectorySelectorActionListener implements FileSelectorActionListener {
    private final App app;
    private final FileSelectorPanel panel;

    public OutputDirectorySelectorActionListener() {
        this(null, null);
    }

    private OutputDirectorySelectorActionListener(App app, FileSelectorPanel panel) {
        this.app = app;
        this.panel = panel;
    }

    @Override
    public FileSelectorActionListener from(App app, FileSelectorPanel panel) {
        return new OutputDirectorySelectorActionListener(app, panel);
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        JFileChooser sourceFileChooser = new JFileChooser(panel.getCurrentPath().toFile());
        sourceFileChooser.setDialogTitle("Select Output Directory");
        sourceFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        int returnVal = sourceFileChooser.showDialog(app, "Select Directory");
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            try {
                Path curr = sourceFileChooser.getSelectedFile().toPath();
                while (!curr.toFile().exists() && curr.getParent() != null)
                    curr = curr.getParent();

                if (!curr.toFile().isDirectory())
                    curr = curr.getParent();

                panel.setCurrentPath(curr);
            } catch (SettingsManagerException e) {
                app.errorMessage("Output Directory Selection Error", "There was an issue saving your new output directory. Try again.");
            }
        }
    }
}
