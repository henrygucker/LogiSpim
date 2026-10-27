package org.henrygucker.logispim.fileprocessor.actionlisteners;

import org.henrygucker.logispim.fileprocessor.App;
import org.henrygucker.logispim.fileprocessor.backend.AppConfig;
import org.henrygucker.logispim.fileprocessor.backend.fileutils.ResourceFileUtil;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.file.Path;

public class ReinitializeLogisimCircuitActionListener implements ActionListener {

    private final App app;

    public ReinitializeLogisimCircuitActionListener(App app) {
        this.app = app;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Path workspaceDir = (Path) AppConfig.WORKSPACE.getValue();
        Path circPath = workspaceDir.resolve("LogiSpim.circ").toAbsolutePath().normalize();

        try {
            ResourceFileUtil.copyResourceToLocation("LogiSpim.circ", circPath);
        } catch (IOException ex) {
            app.errorMessage("Error", "An IO issue occurred while reinitializing the Logisim-evolution circuit.");
            return;
        }

        JOptionPane.showMessageDialog(app, "Logisim-evolution circuit successfully reinitialized!", "Reinitialization Successful", JOptionPane.INFORMATION_MESSAGE);
    }
}
