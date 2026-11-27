package org.henrygucker.logispim.application.actionlisteners;

import org.henrygucker.logispim.application.backend.fileutils.FileUtil;
import org.henrygucker.logispim.application.uicomponents.FileSelectorPanel;
import org.henrygucker.logispim.application.App;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
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
