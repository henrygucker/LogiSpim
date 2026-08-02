package org.henrygucker.logispim.fileprocessor;

import org.henrygucker.logispim.fileprocessor.actionlisteners.CompileActionListener;
import org.henrygucker.logispim.fileprocessor.actionlisteners.KernelTextCheckboxActionListener;
import org.henrygucker.logispim.fileprocessor.backend.SettingsManager;
import org.henrygucker.logispim.fileprocessor.backend.exceptions.SettingsManagerException;
import org.henrygucker.logispim.fileprocessor.uicomponents.FileSelectorPanel;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

public class App extends JFrame {

    private static final int WIDTH = 1080;
    private static final int HEIGHT = 800;
    public static final int CONTENT_WIDTH = 640;

    private final GridBagLayout layout;
    private final Path homePath;
    private SettingsManager settingsManager;
    private final JTextArea textArea;

    public App() {
        // App start

        super("LogiSpim");

        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        this.setMinimumSize(new Dimension(WIDTH, HEIGHT));
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


        homePath = Paths.get(System.getProperty("user.home") + File.separator + "LogiSpim");

        try {
            settingsManager = new SettingsManager(homePath.resolve("settings.xml"));
        } catch (SettingsManagerException e) {
            settingsManager = null;
           errorMessage("Initialization Error", "There was an issue in creating the initial settings file: \"" + e.getIsolatedMessage() + "\"");
        }

        JPanel mainPanel = new JPanel();
        mainPanel.setSize(new Dimension(WIDTH, HEIGHT));

        this.layout = new GridBagLayout();
        mainPanel.setLayout(layout);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 1;
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.gridwidth = 5;
        constraints.weightx = 0;
        constraints.weighty = 0;

        JLabel titleLabel = new JLabel("LogiSpim");
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        Font font = titleLabel.getFont();
        titleLabel.setFont(font.deriveFont(36.0f));
        titleLabel.setPreferredSize(new Dimension(CONTENT_WIDTH, 100));
        titleLabel.setMinimumSize(new Dimension(CONTENT_WIDTH, 100));

        constraints.gridy = 0;

        layout.setConstraints(titleLabel, constraints);

        mainPanel.add(titleLabel);

        FileSelectorPanel sourcePanel = new FileSelectorPanel(this, FileSelectorPanel.FileSelectorPanelType.SOURCE_FILE);

        constraints.gridy = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        this.layout.setConstraints(sourcePanel, constraints);

        mainPanel.add(sourcePanel);

        FileSelectorPanel outputDirectoryPanel = new FileSelectorPanel(this, FileSelectorPanel.FileSelectorPanelType.OUTPUT_DIRECTORY);

        constraints.gridy = 2;
        this.layout.setConstraints(outputDirectoryPanel, constraints);

        mainPanel.add(outputDirectoryPanel);


        // Compile Button
        JButton compileButton = new JButton("Compile");
        compileButton.addActionListener(new CompileActionListener(this));

        constraints.gridx = 1;
        constraints.gridy = 3;
        constraints.gridwidth = 5;
        constraints.weighty = 0;
        constraints.fill = GridBagConstraints.NONE;
        this.layout.setConstraints(compileButton, constraints);

        mainPanel.add(compileButton);

        // Kernel Text CheckBox
        boolean isKernelText = false;
        try {
            isKernelText = (boolean) getSetting(SettingsManager.Settings.IS_KERNEL_TEXT);
        } catch (SettingsManagerException _) {
            try {
                setSetting(SettingsManager.Settings.IS_KERNEL_TEXT, isKernelText);
            } catch (SettingsManagerException _) {
                // Attempt to sync checkbox state to previous value failed, defaulting to false.
            }
        }
        JCheckBox kernelTextCheckBox = new JCheckBox("Kernel Text", isKernelText);
        kernelTextCheckBox.addActionListener(new KernelTextCheckboxActionListener(this, kernelTextCheckBox));

        Border kernelTextCheckBoxBorder = BorderFactory.createEmptyBorder(0, 0, 6, 0);
        kernelTextCheckBox.setBorder(kernelTextCheckBoxBorder);

        constraints.gridy = 4;

        this.layout.setConstraints(kernelTextCheckBox, constraints);
        mainPanel.add(kernelTextCheckBox);




        // Text Area
        textArea = new JTextArea();
        textArea.setLineWrap(false);
        textArea.setEditable(false);
        textArea.setMargin(new Insets(12, 12, 12, 12));
        textArea.setFont(new Font("Courier", Font.PLAIN, 12));
        textArea.setMinimumSize(new Dimension(CONTENT_WIDTH, 0));
        textArea.setMaximumSize(new Dimension(CONTENT_WIDTH, Integer.MAX_VALUE));

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setMinimumSize(new Dimension(CONTENT_WIDTH, 0));
        scrollPane.setMaximumSize(new Dimension(CONTENT_WIDTH, Integer.MAX_VALUE));

        constraints.gridx = 1;
        constraints.gridy = 5;
        constraints.gridwidth = 5;
        constraints.weighty = 1;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.anchor = GridBagConstraints.NORTH;
        this.layout.setConstraints(scrollPane, constraints);

        mainPanel.add(scrollPane);

        JPanel leftVerticalPadding = new JPanel();
        leftVerticalPadding.setMinimumSize(new Dimension((WIDTH - CONTENT_WIDTH) / 2, HEIGHT));

        JPanel rightVerticalPadding = new JPanel();
        rightVerticalPadding.setMinimumSize(new Dimension((WIDTH - CONTENT_WIDTH) / 2, HEIGHT));

        constraints.gridwidth = 1;
        constraints.gridheight = 6;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 1;
        constraints.gridy = 0;

        layout.setConstraints(leftVerticalPadding, constraints);

        constraints.gridx = 6;

        layout.setConstraints(rightVerticalPadding, constraints);

        mainPanel.add(leftVerticalPadding);
        mainPanel.add(rightVerticalPadding);

        // Final step :)
        this.setContentPane(mainPanel);
        this.validate();
        this.setVisible(true);
    }

    /**
     * Updates the text area with a specified {@link String}.
     * @param text The {@link String} with contents that will be displayed in the {@link JTextArea}.
     */
    public void updateTextArea(String text) {
        textArea.setText(text);
    }


    /**
     * Updates the {@link JTextArea} with text contents of a file at a {@link Path} and displays line numbers.
     * Displays up to 500,000 lines. Otherwise, will display that there were too many lines in the file.
     * @param path The {@link Path} to the file containing text contents that will be displayed.
     */
    public void updateTextAreaWithFileContent(Path path) {
        if (!path.toFile().exists() || path.toFile().isDirectory())
            return;

        final int MAX_LINES = 500_000;
        final int MAX_LINE_COUNT_WIDTH = 6;
        int count = 0;
        String inn;
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(path.toFile()))) {

            while ((inn = reader.readLine()) != null) {
                count++;
                builder.append(String.format("%06d ", count));
                builder.append(inn);
                builder.append('\n');

                if (count > MAX_LINES) {
                    updateTextArea(
                            "File \"" + path.toAbsolutePath().toString() + "\" is too long to display.\n" +
                                    "Line count exceeds 500,000."
                    );
                    return;
                }
            }

            updateTextArea(builder.toString());

        } catch (FileNotFoundException _) {
            // This case would never happen since the method exits if the file does not exist.
            return;
        } catch (IOException e) {
            updateTextArea("There was an issue when reading the contents of this file.");
        }
    }

    /**
     * Gets the home path of the program data.
     * Default: user.home/LogiSpim
     * @return The {@link Path} to the home directory of the program data.
     */
    public Path getHomePath() {
        return homePath.toAbsolutePath();
    }

    /**
     * Displays error message with {@link JOptionPane}.
     * @param title The title of the error message pane.
     * @param message The message contents of the error message pane.
     */
    public void errorMessage(String title, String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                title,
                JOptionPane.ERROR_MESSAGE
        );
    }

    /**
     * Gets the stored setting.
     * @param setting The {@link SettingsManager.Settings} value to retrieve.
     * @return The stored value that is retrieved. Will return {@code null} if value cannot be parsed.
     * @throws SettingsManagerException if an issue occurs with IO, or if an issue occurs while resetting the settings file to default values.
     */
    public Object getSetting(SettingsManager.Settings setting) throws SettingsManagerException {
        return settingsManager.get(setting);
    }

    /**
     *
     * @param setting Which {@link SettingsManager.Settings} value to update.
     * @param value
     * @throws SettingsManagerException
     */
    public void setSetting(SettingsManager.Settings setting, Object value) throws SettingsManagerException {
        //TODO: FINISH DOC COMMENTS
        settingsManager.set(setting, value);
    }
}
