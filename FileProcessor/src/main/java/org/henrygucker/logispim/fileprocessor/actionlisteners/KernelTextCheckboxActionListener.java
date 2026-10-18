package org.henrygucker.logispim.fileprocessor.actionlisteners;

import org.henrygucker.logispim.fileprocessor.backend.AppConfig;
import org.henrygucker.logispim.fileprocessor.backend.exceptions.AppConfigException;
import org.henrygucker.logispim.fileprocessor.App;

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
            // Should never throw an exception due to type conformity
            AppConfig.IS_KERNEL_TEXT.setValue(checkBox.isSelected());
        } catch (AppConfigException _) {
            checkBox.setSelected(!checkBox.isSelected());
        }
    }
}
