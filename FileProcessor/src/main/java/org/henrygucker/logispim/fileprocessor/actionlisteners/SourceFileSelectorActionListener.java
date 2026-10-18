package org.henrygucker.logispim.fileprocessor.actionlisteners;

import org.henrygucker.logispim.fileprocessor.backend.AppConfig;
import org.henrygucker.logispim.fileprocessor.backend.exceptions.AppConfigException;
import org.henrygucker.logispim.fileprocessor.uicomponents.FileSelectorPanel;
import org.henrygucker.logispim.fileprocessor.App;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;

public class SourceFileSelectorActionListener implements FileSelectorActionListener {
    private final App app;
    private final FileSelectorPanel panel;

    public SourceFileSelectorActionListener() {
        this(null, null);
    }

    private SourceFileSelectorActionListener(App app, FileSelectorPanel panel) {
        this.app = app;
        this.panel = panel;
    }

    @Override
    public FileSelectorActionListener from(App app, FileSelectorPanel panel) {
        return new SourceFileSelectorActionListener(app, panel);
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        JFileChooser sourceFileChooser = new JFileChooser(((Path) AppConfig.LAST_CHOSEN_SOURCE_FILE_PATH.getValue()).toFile());
        FileNameExtensionFilter asmFileFilter = new FileNameExtensionFilter("MIPS Assembly Files (.s)", "s");
        sourceFileChooser.setFileFilter(asmFileFilter);

        int returnVal = sourceFileChooser.showDialog(app, "Select");
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            Path selectedPath = sourceFileChooser.getSelectedFile().toPath();

            // Updates config value
            try {
                AppConfig.LAST_CHOSEN_SOURCE_FILE_PATH.setValue(selectedPath);
            } catch (AppConfigException _) {} // selectedPath is of type Path

            // Updates panel text field and text area with file contents
            panel.setCurrentPath(selectedPath);
            app.updateTextAreaWithFileContent(selectedPath);
        }
    }
}
