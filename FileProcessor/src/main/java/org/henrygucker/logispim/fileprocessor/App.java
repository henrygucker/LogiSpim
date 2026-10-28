package org.henrygucker.logispim.fileprocessor;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.util.SystemInfo;
import org.henrygucker.logispim.fileprocessor.actionlisteners.CompileActionListener;
import org.henrygucker.logispim.fileprocessor.actionlisteners.KernelTextCheckboxActionListener;
import org.henrygucker.logispim.fileprocessor.actionlisteners.OpenLogisimCircuitActionListener;
import org.henrygucker.logispim.fileprocessor.actionlisteners.ReinitializeLogisimCircuitActionListener;
import org.henrygucker.logispim.fileprocessor.backend.AppConfig;
import org.henrygucker.logispim.fileprocessor.uicomponents.FileSelectorPanel;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.io.*;
import java.net.URL;
import java.nio.file.Path;

public class App extends JFrame {

    // Window Dimensions
    private static final int MIN_WINDOW_WIDTH = 720;
    private static final int MIN_WINDOW_HEIGHT = 600;

    private static final int PREFERRED_WINDOW_WIDTH = 960;
    private static final int PREFERRED_WINDOW_HEIGHT = 1080;

    public static final int CONTENT_WIDTH = 700;

    // Main Column Swing Components
    private static final int MAIN_COLUMN_GRID_WIDTH = 3;
    private final JLabel titleLabel;
    private final FileSelectorPanel sourceSelectorPanel;
    private final FileSelectorPanel workspaceSelectorPanel;
    private final JButton compileButton;
    private final JButton openLogisimCircButton;
    private final JButton reinitializeLogisimCircButton;
    private final JCheckBox kernelCheckBox;
    private final JScrollPane textAreaScrollPane;
    private final JTextArea textArea;

    private final JPanel mainColumnPanel;
    private final GridBagLayout mainColumnPanelLayout;

    private final JPanel columnWrapperPanel;

    public final Font defaultBaseFont;

    public App(String appName) {
        // App start
        super(appName);

        // Set look and feel (dark mode)
        initLookAndFeel();
        defaultBaseFont = initFont();

        // Set window properties
        this.setSize(new Dimension(PREFERRED_WINDOW_WIDTH, PREFERRED_WINDOW_HEIGHT)); // Default size
        this.setMinimumSize(new Dimension(MIN_WINDOW_WIDTH, MIN_WINDOW_HEIGHT)); // Smallest allowed size
        this.setPreferredSize(new Dimension(PREFERRED_WINDOW_WIDTH, PREFERRED_WINDOW_HEIGHT)); // Preferred size
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Init main column panel & layout
        mainColumnPanel = new JPanel();
        mainColumnPanel.setSize(new Dimension(CONTENT_WIDTH, PREFERRED_WINDOW_HEIGHT));

        mainColumnPanelLayout = new GridBagLayout();
        mainColumnPanel.setLayout(mainColumnPanelLayout);

        // Init items in main column panel & place them
        titleLabel = initTitleLabel();
        workspaceSelectorPanel = initWorkspaceSelectorPanel();
        sourceSelectorPanel = initSourceSelectorPanel();

        JSeparator separator = initSeparator();

        compileButton = initCompileButton();
        openLogisimCircButton = initOpenLogisimCircButton();
        reinitializeLogisimCircButton = initReinitializeLogisimCircButton();
        kernelCheckBox = initKernelCheckBox();
        textArea = initTextArea();
        textAreaScrollPane = initTextAreaScrollPane();

        // Initialize columnWrapperPanel (primary content pane)
        columnWrapperPanel = initColumnWrapperPanel();

        // Final step
        this.setContentPane(columnWrapperPanel);
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
     * Refreshes all file selector panels with the most recently stored path
     */
    public void refreshFileSelectors() {
        // Update source file selector panel
        this.sourceSelectorPanel.setCurrentPath((Path) AppConfig.SOURCE_FILE.getValue());

        // Update workspace selector panel
        this.workspaceSelectorPanel.setCurrentPath((Path) AppConfig.WORKSPACE.getValue());
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

    private void initLookAndFeel() {
        // Initialize theme
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (Exception ex) {
            System.err.println("Failed to initialize FlatLaf Dark theme.");
        }

        // Adjust global component arc size (corner rounding)
        UIManager.put("Component.arc", 20);      // General components
        UIManager.put("TextComponent.arc", 20);  // JTextField, JPasswordField, etc.
        UIManager.put("Button.arc", 20);         // JButton
        UIManager.put("CheckBox.arc", 5);        // JCheckBox
        UIManager.put("ProgressBar.arc", 10);    // JProgressBar

        // macOS-specific visual configuration (removing title bar)
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("mac")) {
            if (SystemInfo.isMacFullWindowContentSupported ) {
                // Removes white bar at the top of window (allows full window content)
                this.getRootPane().putClientProperty( "apple.awt.fullWindowContent", true );
                this.getRootPane().putClientProperty( "apple.awt.transparentTitleBar", true );
            }

            // Removes title name from top of window
            this.getRootPane().putClientProperty("apple.awt.windowTitleVisible", false );
        }
    }

    private Font initFont() {
        Font baseFont = null;

        // Load custom font
        try(InputStream is = ClassLoader.getSystemResourceAsStream("Nunito-VariableFont_wght.ttf")) {
            if (is == null)
                System.err.println("Font file not found in resources.");

            baseFont = Font.createFont(Font.TRUETYPE_FONT, is);
            Font font = baseFont.deriveFont(Font.BOLD, 12f);

            UIManager.put("defaultFont", font);
        } catch (FontFormatException | IOException e) {
            System.err.println("An issue occurred while loading font.");
            e.printStackTrace();
        }

        return baseFont;
    }

    private JLabel initTitleLabel() {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = MAIN_COLUMN_GRID_WIDTH;
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.ipady = 20;

        URL titleImageURL = ClassLoader.getSystemResource("LogiSpim_Logo_Dark.png");
        ImageIcon titleImage = new ImageIcon(titleImageURL);
        Image scaledImg = titleImage.getImage().getScaledInstance(CONTENT_WIDTH / 2, -1, Image.SCALE_SMOOTH);
        titleImage = new ImageIcon(scaledImg);

        JLabel titleLabel = new JLabel(titleImage);
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        titleLabel.setMinimumSize(new Dimension(CONTENT_WIDTH, -1));

        mainColumnPanelLayout.setConstraints(titleLabel, constraints);

        mainColumnPanel.add(titleLabel);

        return titleLabel;
    }

    private FileSelectorPanel initWorkspaceSelectorPanel() {
        FileSelectorPanel workspaceSelectorPanel = new FileSelectorPanel(this, FileSelectorPanel.FileSelectorPanelType.WORKSPACE_DIRECTORY);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridy = 1;
        constraints.gridwidth = MAIN_COLUMN_GRID_WIDTH;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(0, 0, 10, 0);
        mainColumnPanelLayout.setConstraints(workspaceSelectorPanel, constraints);

        mainColumnPanel.add(workspaceSelectorPanel);

        return workspaceSelectorPanel;
    }

    private FileSelectorPanel initSourceSelectorPanel() {
        FileSelectorPanel sourceSelectorPanel = new FileSelectorPanel(this, FileSelectorPanel.FileSelectorPanelType.SOURCE_FILE);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridy = 2;
        constraints.gridwidth = MAIN_COLUMN_GRID_WIDTH;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(0, 0, 10, 0);
        mainColumnPanelLayout.setConstraints(sourceSelectorPanel, constraints);

        mainColumnPanel.add(sourceSelectorPanel);

        return sourceSelectorPanel;
    }

    private JSeparator initSeparator() {
        JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridy = 3;
        constraints.gridwidth = MAIN_COLUMN_GRID_WIDTH;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(5, 0, 15, 0);
        mainColumnPanelLayout.setConstraints(separator, constraints);

        mainColumnPanel.add(separator);

        return separator;
    }

    private JButton initCompileButton() {
        JButton compileButton = new JButton("Compile");
        compileButton.addActionListener(new CompileActionListener(this));

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridy = 4;
        constraints.gridx = 0;
        constraints.weightx = 1;
        constraints.insets = new Insets(0, 0, 10, 0);
        constraints.anchor = GridBagConstraints.EAST;
        mainColumnPanelLayout.setConstraints(compileButton, constraints);

        mainColumnPanel.add(compileButton);

        return compileButton;
    }

    private JButton initOpenLogisimCircButton() {
        JButton openLogisimCircButton = new JButton("Open Logisim");
        openLogisimCircButton.addActionListener(new OpenLogisimCircuitActionListener(this));

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridy = 4;
        constraints.gridx = 1;
        constraints.insets = new Insets(0, 5, 10, 0);
        mainColumnPanelLayout.setConstraints(openLogisimCircButton, constraints);

        mainColumnPanel.add(openLogisimCircButton);

        return openLogisimCircButton;
    }

    private JButton initReinitializeLogisimCircButton() {
        JButton reinitLogisimCircButton = new JButton("Reinitialize Circuit");
        reinitLogisimCircButton.addActionListener(new ReinitializeLogisimCircuitActionListener(this));

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridy = 4;
        constraints.gridx = 2;
        constraints.weightx = 1;
        constraints.insets = new Insets(0, 5, 10, 0);
        constraints.anchor = GridBagConstraints.WEST;
        mainColumnPanelLayout.setConstraints(reinitLogisimCircButton, constraints);

        mainColumnPanel.add(reinitLogisimCircButton);

        return reinitLogisimCircButton;
    }

    private JCheckBox initKernelCheckBox() {
        // Fetch stored state
        boolean isKernelText = (boolean) AppConfig.COMPILE_KERNEL.getValue();

        JCheckBox kernelTextCheckBox = new JCheckBox("Compile as Kernel", isKernelText);
        kernelTextCheckBox.addActionListener(new KernelTextCheckboxActionListener(this, kernelTextCheckBox));

        Border kernelTextCheckBoxBorder = BorderFactory.createEmptyBorder(0, 0, 10, 0);
        kernelTextCheckBox.setBorder(kernelTextCheckBoxBorder);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridy = 5;
        constraints.gridwidth = MAIN_COLUMN_GRID_WIDTH;
        mainColumnPanelLayout.setConstraints(kernelTextCheckBox, constraints);

        mainColumnPanel.add(kernelTextCheckBox);

        return kernelTextCheckBox;
    }

    private JTextArea initTextArea() {
        JTextArea textArea = new JTextArea();
        textArea.setLineWrap(false);
        textArea.setEditable(false);
        textArea.setMargin(new Insets(12, 12, 12, 12));
        textArea.setFont(new Font("Courier", Font.PLAIN, 12));

        return textArea;
    }

    private JScrollPane initTextAreaScrollPane() {
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setPreferredSize(new Dimension(CONTENT_WIDTH, HEIGHT));
        scrollPane.createVerticalScrollBar();

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridy = 6;
        constraints.weighty = 1.0;
        constraints.gridwidth = MAIN_COLUMN_GRID_WIDTH;
        constraints.anchor = GridBagConstraints.NORTH;
        constraints.fill = GridBagConstraints.BOTH;
        mainColumnPanelLayout.setConstraints(scrollPane, constraints);

        mainColumnPanel.add(scrollPane);

        return scrollPane;
    }

    private JPanel initColumnWrapperPanel() {
        // Creating left and right padding panels
        JPanel leftVerticalPadding = new JPanel();
        leftVerticalPadding.setPreferredSize(new Dimension((MIN_WINDOW_WIDTH - CONTENT_WIDTH) / 2, MIN_WINDOW_HEIGHT));

        JPanel rightVerticalPadding = new JPanel();
        rightVerticalPadding.setPreferredSize(new Dimension((MIN_WINDOW_WIDTH - CONTENT_WIDTH) / 2, MIN_WINDOW_HEIGHT));

        // Merging the three column panels together into the columnWrapperPanel
        JPanel outColumnWrapperPanel = new JPanel();

        GridBagLayout layout = new GridBagLayout();
        outColumnWrapperPanel.setLayout(layout);

        // Left panel constraints
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weighty = 1;
        constraints.weightx = 1.0;
        constraints.fill = GridBagConstraints.BOTH;

        // Apply to left panel and add left panel to layout
        layout.setConstraints(leftVerticalPadding, constraints);
        outColumnWrapperPanel.add(leftVerticalPadding);

        // Right panel constraints
        constraints.gridx = 2;

        // Apply to right panel and add right panel to layout
        layout.setConstraints(rightVerticalPadding, constraints);
        outColumnWrapperPanel.add(rightVerticalPadding);

        // Main panel constraints
        constraints.gridx = 1;
        constraints.weightx = 0;
        constraints.fill = GridBagConstraints.VERTICAL;

        // Apply to main panel and add main panel to layout
        layout.setConstraints(mainColumnPanel, constraints);
        outColumnWrapperPanel.add(mainColumnPanel);

        return outColumnWrapperPanel;
    }
}
