package org.henrygucker.logimips.fileprocessor.actionlisteners;

import org.henrygucker.logimips.fileprocessor.backend.exceptions.SettingsManagerException;
import org.henrygucker.logimips.fileprocessor.uicomponents.FileSelectorPanel;
import org.henrygucker.logimips.fileprocessor.App;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;

public class SourceFileSelectorActionListener implements FileSelectorActionListener {
    private final App app;
    private final FileSelectorPanel panel;

    public SourceFileSelectorActionListener() {
        this(null, null);
    }

    private SourceFileSelectorActionListener(App app, FileSelectorPanel panel) {
        this.app = app;
        this.panel = panel;
    }

    @Override
    public FileSelectorActionListener from(App app, FileSelectorPanel panel) {
        return new SourceFileSelectorActionListener(app, panel);
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        JFileChooser sourceFileChooser = new JFileChooser(panel.getCurrentPath().toFile());
        FileNameExtensionFilter asmFileFilter = new FileNameExtensionFilter("MIPS Assembly Files (.s)", "s");
        sourceFileChooser.setFileFilter(asmFileFilter);

        int returnVal = sourceFileChooser.showDialog(app, "Select");
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            try {
                panel.setCurrentPath(sourceFileChooser.getSelectedFile().toPath());
                app.updateTextAreaWithFileContent(sourceFileChooser.getSelectedFile().toPath());
            } catch (SettingsManagerException e) {
                app.errorMessage("File Selection Error", "There was an issue saving your file selection. Try again.");
            }
        }
    }

    private void updateTextAreaWithFileContent(Path path) {
        if (!path.toFile().exists() || path.toFile().isDirectory())
            return;

        int linesUntilTooMany = 500_000;
        String inn;
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(path.toFile()))) {
            while ((inn = reader.readLine()) != null) {
                builder.append(inn);
                builder.append('\n');

                linesUntilTooMany--;
                if (linesUntilTooMany <= 0) {
                    app.updateTextArea(
                            "File \"" + path.toAbsolutePath().toString() + "\" is too long to display.\n" +
                            "Line count exceeds 500,000."
                    );
                    return;
                }
            }

            app.updateTextArea(builder.toString());

        } catch (FileNotFoundException _) {
            // This case would never happen since the method exits if the file does not exist.
            return;
        } catch (IOException e) {
            app.updateTextArea("There was an issue when reading the contents of this file.");
        }
    }
}
