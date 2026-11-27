package org.henrygucker.logispim.application.actionlisteners;

import org.henrygucker.logispim.application.uicomponents.FileSelectorPanel;
import org.henrygucker.logispim.application.App;

import java.awt.event.ActionListener;

public interface FileSelectorActionListener extends ActionListener {
    public FileSelectorActionListener from(App app, FileSelectorPanel panel);
}
