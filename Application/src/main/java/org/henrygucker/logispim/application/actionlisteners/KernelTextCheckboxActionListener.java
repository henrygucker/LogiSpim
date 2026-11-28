package org.henrygucker.logispim.application.actionlisteners;

import org.henrygucker.logispim.application.backend.AppConfig;
import org.henrygucker.logispim.application.backend.exceptions.AppConfigException;
import org.henrygucker.logispim.application.App;

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
            AppConfig.COMPILE_KERNEL.setValue(checkBox.isSelected());
        } catch (AppConfigException _) {
            checkBox.setSelected(!checkBox.isSelected());
        }
    }
}
