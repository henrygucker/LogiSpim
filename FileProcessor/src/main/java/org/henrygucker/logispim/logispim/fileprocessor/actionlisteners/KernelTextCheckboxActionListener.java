package org.henrygucker.logispim.logispim.fileprocessor.actionlisteners;

import org.henrygucker.logispim.logispim.fileprocessor.backend.SettingsManager;
import org.henrygucker.logispim.logispim.fileprocessor.backend.exceptions.SettingsManagerException;
import org.henrygucker.logispim.logispim.fileprocessor.App;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class KernelTextCheckboxActionListener implements ActionListener {
    App app;
    JCheckBox checkBox;

    public KernelTextCheckboxActionListener(App app, JCheckBox checkBox) {
        this.app = app;
        this.checkBox = checkBox;
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            app.setSetting(SettingsManager.Settings.IS_KERNEL_TEXT, checkBox.isSelected());
        } catch (SettingsManagerException _) {
            checkBox.setSelected(!checkBox.isSelected());
        }
    }
}
