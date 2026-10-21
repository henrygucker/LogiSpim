package org.henrygucker.logispim.fileprocessor.actionlisteners;

import org.henrygucker.logispim.fileprocessor.backend.AppConfig;
import org.henrygucker.logispim.fileprocessor.backend.exceptions.AppConfigException;
import org.henrygucker.logispim.fileprocessor.uicomponents.FileSelectorPanel;
import org.henrygucker.logispim.fileprocessor.App;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
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
        JFileChooser sourceFileChooser = new JFileChooser(((Path) AppConfig.SOURCE_FILE.getValue()).toFile());
        FileNameExtensionFilter asmFileFilter = new FileNameExtensionFilter("MIPS Assembly Files (.s)", "s");
        sourceFileChooser.setFileFilter(asmFileFilter);

        int returnVal = sourceFileChooser.showDialog(app, "Select");
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            Path selectedPath = sourceFileChooser.getSelectedFile().toPath();

            // Updates config value
            try {
                AppConfig.SOURCE_FILE.setValue(selectedPath);
            } catch (AppConfigException _) {} // selectedPath is of type Path

            // Updates panel text field and text area with file contents
            panel.setCurrentPath(selectedPath);
            app.updateTextAreaWithFileContent(selectedPath);
        }
    }
}
