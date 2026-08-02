package org.henrygucker.logispim.fileprocessor.actionlisteners;

import org.henrygucker.logispim.fileprocessor.uicomponents.FileSelectorPanel;
import org.henrygucker.logispim.fileprocessor.App;

import java.awt.event.ActionListener;

public interface FileSelectorActionListener extends ActionListener {
    public FileSelectorActionListener from(App app, FileSelectorPanel panel);
}
