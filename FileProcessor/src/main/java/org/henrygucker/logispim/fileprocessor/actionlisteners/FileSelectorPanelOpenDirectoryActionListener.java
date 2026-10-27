package org.henrygucker.logispim.fileprocessor.actionlisteners;

import org.henrygucker.logispim.fileprocessor.backend.AppConfig;
import org.henrygucker.logispim.fileprocessor.backend.WorkspaceFileHandler;
import org.henrygucker.logispim.fileprocessor.backend.fileutils.FileUtil;
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

        FileUtil.openDirectory(app, currentFile.toPath());
    }
}
