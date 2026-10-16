package org.henrygucker.logispim.fileprocessor.uicomponents;

import org.henrygucker.logispim.fileprocessor.actionlisteners.FileSelectorActionListener;
import org.henrygucker.logispim.fileprocessor.actionlisteners.OutputDirectorySelectorActionListener;
import org.henrygucker.logispim.fileprocessor.actionlisteners.SourceFileSelectorActionListener;
import org.henrygucker.logispim.fileprocessor.backend.AppConfig;
import org.henrygucker.logispim.fileprocessor.backend.exceptions.AppConfigException;
import org.henrygucker.logispim.fileprocessor.App;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.file.Path;

public class FileSelectorPanel extends JPanel {
    public enum FileSelectorPanelType {
        SOURCE_FILE(
                "Source File",
                AppConfig.SOURCE_FILE_ABSOLUTE_PATH,
                "LogiSpim",
                new SourceFileSelectorActionListener()
                ),
        OUTPUT_DIRECTORY(
                "Output Directory",
                AppConfig.OUTPUT_DIRECTORY_ABSOLUTE_PATH,
                "LogiSpim" + File.separator + "out",
                new OutputDirectorySelectorActionListener()
                );

        private final String name;
        private final AppConfig appConfig;
        private final String defaultPathRelativeFromHome;
        private final FileSelectorActionListener actionListenerEmptyInstance;

        FileSelectorPanelType(String name, AppConfig appConfig, String defaultPathRelativeFromHome, FileSelectorActionListener actionListenerEmptyInstance) throws IllegalArgumentException {
            this.name = name;
            this.appConfig = appConfig;
            this.defaultPathRelativeFromHome = defaultPathRelativeFromHome;
            this.actionListenerEmptyInstance = actionListenerEmptyInstance;
        }

        public String getName() {
            return name;
        }

        public AppConfig getAssociatedSetting() {
            return appConfig;
        }

        public Path getDefaultPath(App app) {
            return app.getHomePath().resolve(defaultPathRelativeFromHome);
        }

        public ActionListener getActionListener(App app, FileSelectorPanel panel) {
            return this.actionListenerEmptyInstance.from(app, panel);
        }
    }

    private final App app;
    private final FileSelectorPanelType type;
    private final JTextField textField;
    private final JButton change;

    public FileSelectorPanel(App app, FileSelectorPanelType type) {
        super();
        this.app = app;
        this.type = type;
        GridBagLayout layout = new GridBagLayout();
        this.setLayout(layout);

        GridBagConstraints constraints = new GridBagConstraints();

        JLabel label = new JLabel(type.name);
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 2;
        constraints.weightx = 1;
        constraints.weighty = 1;
        constraints.fill = GridBagConstraints.REMAINDER;
        constraints.anchor = GridBagConstraints.SOUTH;

        layout.setConstraints(label, constraints);
        this.add(label);

        ActionListener actionListener = type.getActionListener(app, this);

        change = new JButton("Change");
        change.addActionListener(actionListener);

        constraints.gridwidth = 1;
        constraints.gridx = 1;
        constraints.gridy = 1;
        constraints.weightx = 0;
        constraints.weighty = 0;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.EAST;
        layout.setConstraints(change, constraints);
        this.add(change);

        textField = new JTextField(getCurrentPath().toAbsolutePath().toString());
        textField.setEditable(false);

        constraints.gridx = 0;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.WEST;

        layout.setConstraints(textField, constraints);
        this.add(textField);
    }


    /**
     * Retrieves the {@link Path} to the most recently accessed value.
     * @return the {@link Path} to either the most recently selected value, or the default value.
     */
    public Path getCurrentPath() {
        Path path = (Path) type.getAssociatedSetting().getValue();

        if (path == null)
            path = type.getDefaultPath(app);

        return path;
    }

    public void setCurrentPath(Path path) throws AppConfigException {
        // If issue with updating setting occurs, SettingsManagerException will be thrown here
        //TODO: FINISH DOC COMMENTS
        type.getAssociatedSetting().setValue(path);

        // Text field will not update if issue occurs with settings update
        textField.setText(path.toAbsolutePath().toString());
    }
}
