package org.henrygucker.logispim.fileprocessor.uicomponents;

import org.henrygucker.logispim.fileprocessor.actionlisteners.FileSelectorActionListener;
import org.henrygucker.logispim.fileprocessor.actionlisteners.FileSelectorPanelOpenDirectoryActionListener;
import org.henrygucker.logispim.fileprocessor.actionlisteners.WorkspaceDirectorySelectorActionListener;
import org.henrygucker.logispim.fileprocessor.actionlisteners.SourceFileSelectorActionListener;
import org.henrygucker.logispim.fileprocessor.backend.AppConfig;
import org.henrygucker.logispim.fileprocessor.App;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.nio.file.Path;

public class FileSelectorPanel extends JPanel {
    public enum FileSelectorPanelType {
        SOURCE_FILE(
                "Source File",
                AppConfig.SOURCE_FILE,
                new SourceFileSelectorActionListener()
                ),
        WORKSPACE_DIRECTORY(
                "Workspace Directory",
                AppConfig.WORKSPACE,
                new WorkspaceDirectorySelectorActionListener()
                );

        private final String name;
        private final AppConfig appConfig;
        private final FileSelectorActionListener actionListenerEmptyInstance;

        FileSelectorPanelType(String name, AppConfig appConfig, FileSelectorActionListener actionListenerEmptyInstance) throws IllegalArgumentException {
            this.name = name;
            this.appConfig = appConfig;
            this.actionListenerEmptyInstance = actionListenerEmptyInstance;
        }

        public String getName() {
            return name;
        }

        public AppConfig getAssociatedSetting() {
            return appConfig;
        }

        public Path getDefaultPath() {
            return Path.of(this.getAssociatedSetting().getDefaultValue());
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
        // Object initialization
        super();
        this.app = app;
        this.type = type;

        // Layout initialization
        GridBagLayout layout = new GridBagLayout();
        this.setLayout(layout);

        GridBagConstraints constraints = new GridBagConstraints();

        // Configure label and placement
        JLabel label = new JLabel(type.name);
        label.setFont(app.defaultBaseFont.deriveFont(Font.BOLD, 15.0f));

        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 3;
        constraints.weightx = 1;
        constraints.weighty = 1;
        constraints.fill = GridBagConstraints.REMAINDER;
        constraints.anchor = GridBagConstraints.SOUTH;

        layout.setConstraints(label, constraints);
        this.add(label);

        // Configure change button and placement
        change = new JButton("Change");

        ActionListener actionListener = type.getActionListener(app, this);
        change.addActionListener(actionListener);

        constraints.gridwidth = 1;
        constraints.gridx = 1;
        constraints.gridy = 1;
        constraints.weightx = 0;
        constraints.weighty = 0;
        constraints.insets = new Insets(0, 5, 0, 0);
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.EAST;
        layout.setConstraints(change, constraints);
        this.add(change);

        // Configure open dir button and placement
        JButton open = new JButton("Open");

        actionListener = new FileSelectorPanelOpenDirectoryActionListener(app, this.type);
        open.addActionListener(actionListener);

        constraints.gridx = 2;
        layout.setConstraints(open, constraints);
        this.add(open);


        // Configure text field and placement
        textField = new JTextField(((Path) type.getAssociatedSetting().getValue()).toAbsolutePath().normalize().toString());
        textField.setEditable(false);

        constraints.gridx = 0;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.WEST;

        layout.setConstraints(textField, constraints);
        this.add(textField);
    }


    /**
     * Sets the {@link Path} stored by this {@link FileSelectorPanel}, updating the text field
     * @param path The new {@link Path} to store
     */
    public void setCurrentPath(Path path) {
        textField.setText(path.toAbsolutePath().normalize().toString());
    }
}
