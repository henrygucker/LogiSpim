package org.henrygucker.logispim.application;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.util.SystemInfo;
import org.henrygucker.logispim.application.actionlisteners.CompileActionListener;
import org.henrygucker.logispim.application.actionlisteners.KernelTextCheckboxActionListener;
import org.henrygucker.logispim.application.actionlisteners.OpenLogisimCircuitActionListener;
import org.henrygucker.logispim.application.actionlisteners.ReinitializeLogisimCircuitActionListener;
import org.henrygucker.logispim.application.backend.AppConfig;
import org.henrygucker.logispim.application.uicomponents.FileSelectorPanel;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.io.*;
import java.net.URL;
import java.nio.file.Path;

public class App extends JFrame {

    // Window Dimensions
    public static final int CONTENT_WIDTH = 700;

    private static final int MIN_WINDOW_HEIGHT = 600;

    private static final int MIN_WINDOW_WIDTH = CONTENT_WIDTH + 20;

    private static final int PREFERRED_WINDOW_WIDTH = 960;
    private static final int PREFERRED_WINDOW_HEIGHT = 1080;


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
    public final JTextPane textPane;

    private final JPanel mainColumnPanel;
    private final GridBagLayout mainColumnPanelLayout;

    private final JPanel columnWrapperPanel;

    public final Font defaultBaseFont;
    public final Style baseTextStyle;
    public final Style inlinedTextStyle;
    public final Style lineNumberStyle;

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
        mainColumnPanel.setMinimumSize(new Dimension(MIN_WINDOW_WIDTH, MIN_WINDOW_HEIGHT));

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

        textPane = initTextPane();

        // Initializing styles (to be refactored into its own methods)
        StyledDocument doc = textPane.getStyledDocument();
        baseTextStyle = doc.addStyle("base", null);
        StyleConstants.setFontFamily(baseTextStyle, "Courier");
        StyleConstants.setFontSize(baseTextStyle, 12);
        StyleConstants.setForeground(baseTextStyle, Color.WHITE);

        inlinedTextStyle = doc.addStyle("InlinedText", baseTextStyle);
        StyleConstants.setForeground(inlinedTextStyle, new Color(0x4A7C30));

        lineNumberStyle = doc.addStyle("LineNumber", baseTextStyle);
        StyleConstants.setForeground(lineNumberStyle, Color.GRAY);

        textAreaScrollPane = initTextAreaScrollPane();

        // Initialize columnWrapperPanel (primary content pane)
        columnWrapperPanel = initColumnWrapperPanel();

        // Final step
        this.setContentPane(columnWrapperPanel);
        this.validate();
        this.setVisible(true);
    }

    /**
     * Updates the text pane contents with a specified {@link String}.
     * @param text The {@link String} with contents that will be displayed in the {@link JTextArea}.
     */
    public void updateTextPane(String text) {
        textPane.setText(text);
    }


    /**
     * Updates the {@link JTextArea} with text contents of a file at a {@link Path} and displays line numbers.
     * Displays up to 500,000 lines. Otherwise, will display that there were too many lines in the file.
     * @param path The {@link Path} to the file containing text contents that will be displayed.
     */
    public void updateTextAreaWithFileContent(Path path) {
        if (!path.toFile().exists() || path.toFile().isDirectory())
            return;


        // Reset text pane & get doc
        textPane.setText("");
        StyledDocument doc = textPane.getStyledDocument();

        final int MAX_LINES = 500_000;
        final int MAX_LINE_COUNT_WIDTH = 6;
        int count = 0;
        String inn;
        try (BufferedReader reader = new BufferedReader(new FileReader(path.toFile()))) {

            doc.insertString(doc.getLength(),  path.getFileName().toString() + "\n", lineNumberStyle);

            while ((inn = reader.readLine()) != null) {
                count++;

                doc.insertString(doc.getLength(), String.format("%4d ", count), lineNumberStyle);
                doc.insertString(doc.getLength(), inn, baseTextStyle);
                doc.insertString(doc.getLength(), "\n", baseTextStyle);

                if (count > MAX_LINES) {
                    doc.insertString(
                            doc.getLength(),
                            "\n" +
                                    "File \"" + path.toAbsolutePath().toString() + "\" is too long to display.\n" +
                                    "Content truncated at 500,000 lines.",
                            baseTextStyle
                    );
                    return;
                }
            }

        } catch (FileNotFoundException e) {
            // This case would never happen since the method exits if the file does not exist.
            e.printStackTrace();
        } catch (IOException e) {
            updateTextPane("There was an issue when reading the contents of this file.");
        } catch (BadLocationException e) {
            // This should never happen
            e.printStackTrace();
        }
    }

    /**
     * Refreshes all file selector panels with the most recently stored path.
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

    /**
     * Initializes the look and feel of LogiSpim. Sets theme and any OS-specific configuration values to enhance the
     * app visually.
     */
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

    /**
     * Loads LogiSpim's primary font.
     * @return {@link Font} instance with the primary font's base font
     */
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

    /**
     * Initializes the title label (image) and places it on the {@code mailColumnPanel}.
     * @return {@link JLabel} with the appropriate configuration
     */
    private JLabel initTitleLabel() {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = MAIN_COLUMN_GRID_WIDTH;
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.insets = new Insets(20, 0, 20, 0);

        URL titleImageURL = ClassLoader.getSystemResource("LogiSpim_Logo_Light.png");
        ImageIcon titleImage = new ImageIcon(titleImageURL);
        Image scaledImg = titleImage.getImage().getScaledInstance(MIN_WINDOW_WIDTH / 2, -1, Image.SCALE_SMOOTH);
        titleImage = new ImageIcon(scaledImg);

        JLabel titleLabel = new JLabel(titleImage);
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        titleLabel.setMinimumSize(new Dimension(MIN_WINDOW_WIDTH / 2, -1));

        mainColumnPanelLayout.setConstraints(titleLabel, constraints);

        mainColumnPanel.add(titleLabel);

        return titleLabel;
    }

    /**
     * Initializes the workspace directory selector panel and places it on the {@code mailColumnPanel}.
     * @return {@link FileSelectorPanel} with the appropriate configuration
     */
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

    /**
     * Initializes the source file selector panel and places it on the {@code mailColumnPanel}.
     * @return {@link FileSelectorPanel} with the appropriate configuration
     */
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

    /**
     * Initializes the separator bar which goes between the file selectors and the buttons/text area and places it on the {@code mailColumnPanel}.
     * @return {@link JSeparator} with the appropriate configuration
     */
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

    /**
     * Initializes the button for compiling the currently selected program and places it on the {@code mailColumnPanel}.
     * @return {@link JButton} with the appropriate {@link ActionListener}
     */
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

    /**
     * Initializes the button for launching Logisim-evolution and opening the circuit file and places it on the {@code mailColumnPanel}.
     * @return {@link JButton} with the appropriate {@link ActionListener}
     */
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

    /**
     * Initializes the button for reinitializing the Logisim-evolution circuit file and places it on the {@code mailColumnPanel}.
     * @return {@link JButton}with the appropriate {@link ActionListener}
     */
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

    /**
     * Initializes the Kernel check box and places it on the {@code mailColumnPanel}.
     * @return {@link JCheckBox} linked to {@link AppConfig#COMPILE_KERNEL} config field
     */
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

    /**
     * Initializes the {@link JTextArea}.
     * @return Initialized, but not placed, {@link JTextArea}
     */
    private JTextPane initTextPane() {
        JTextPane textPane = new JTextPane();
        textPane.setEditable(false);
        textPane.setMargin(new Insets(12, 12, 12, 12));
        textPane.setFont(new Font("Courier", Font.PLAIN, 12));
        textPane.setForeground(Color.WHITE);

        return textPane;
    }

    /**
     * Places the {@link JTextArea} widget inside a {@link JScrollPane} to allow for scrolling throughout the text area.
     * Also places the {@link JScrollPane} on the {@code mailColumnPanel}.
     * @return {@link JScrollPane} which wraps the {@link JTextArea}
     */
    private JScrollPane initTextAreaScrollPane() {
        // Wrapping textPane in JPanel to stop text from wrapping
        JPanel noWrapPanel = new JPanel(new BorderLayout());
        noWrapPanel.add(textPane);

        JScrollPane scrollPane = new JScrollPane(noWrapPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setPreferredSize(new Dimension(CONTENT_WIDTH, HEIGHT));
        scrollPane.createVerticalScrollBar();


        // Loads source file if it exists
        Path sourcePath = (Path) AppConfig.SOURCE_FILE.getValue();
        if (sourcePath.toFile().isFile()) {
            this.updateTextAreaWithFileContent(sourcePath);
        }

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridy = 6;
        constraints.weighty = 1.0;
        constraints.insets = new Insets(0, 0, 10, 0);
        constraints.gridwidth = MAIN_COLUMN_GRID_WIDTH;
        constraints.anchor = GridBagConstraints.NORTH;
        constraints.fill = GridBagConstraints.BOTH;
        mainColumnPanelLayout.setConstraints(scrollPane, constraints);

        mainColumnPanel.add(scrollPane);

        return scrollPane;
    }

    /**
     * Initializes the main frame storing each of the three column panels (creates both the left and right padding columns).
     * So the column order from left to right is as follows:<br>
     * 0) Left Padding<br>
     * 1) Main Column<br>
     * 2) Right Padding.<br>
     * <br>
     * This is done to center the content on the window.
     * @return {@link JPanel} of the column wrapper panel
     */
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
