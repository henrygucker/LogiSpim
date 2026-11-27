package org.henrygucker.logispim.application.actionlisteners;

import org.henrygucker.logispim.application.App;
import org.henrygucker.logispim.application.backend.WorkspaceFileHandler;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class ReinitializeLogisimCircuitActionListener implements ActionListener {

    private final App app;

    public ReinitializeLogisimCircuitActionListener(App app) {
        this.app = app;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            WorkspaceFileHandler.writeLogisimCirc();
        } catch (IOException ex) {
            app.errorMessage("Error", "An IO issue occurred while reinitializing the Logisim-evolution circuit.");
            return;
        }

        JOptionPane.showMessageDialog(app, "Logisim-evolution circuit successfully reinitialized!", "Reinitialization Successful", JOptionPane.INFORMATION_MESSAGE);
    }
}
