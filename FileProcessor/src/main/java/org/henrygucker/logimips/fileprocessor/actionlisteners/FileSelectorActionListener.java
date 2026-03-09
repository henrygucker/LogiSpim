package org.henrygucker.logimips.fileprocessor.actionlisteners;

import org.henrygucker.logimips.fileprocessor.uicomponents.FileSelectorPanel;
import org.henrygucker.logimips.fileprocessor.App;

import java.awt.event.ActionListener;

public interface FileSelectorActionListener extends ActionListener {
    public FileSelectorActionListener from(App app, FileSelectorPanel panel);
}
