package org.henrygucker.logispim.fileprocessor;

import com.formdev.flatlaf.FlatDarkLaf;
import org.henrygucker.logispim.fileprocessor.backend.AppConfig;
import org.henrygucker.logispim.fileprocessor.backend.WorkspaceFileHandler;
import org.henrygucker.logispim.fileprocessor.backend.exceptions.AppConfigException;

import javax.naming.ConfigurationException;
import java.io.IOException;
import java.nio.file.Path;

public class Main {
    private static final String VERSION = "0.1.1-beta";

    public static void main(String[] args) throws ConfigurationException {
        // Must be called before any JFrames are created
        FlatDarkLaf.setup();

        validateBinutilsConfig();

        String lastUsedVersion = (String) AppConfig.VERSION.getValue();

        // Check if LogiSpim has never run before
        if (lastUsedVersion.equals("null")) {

            try {
                // Initialize the VERSION config value with the current version
                AppConfig.VERSION.setValue(VERSION);
            } catch (AppConfigException e) {
                // VERSION is always of type String (as is required), so no exceptions are ever thrown
            }

            // Any steps for first initialization of LogiSpim go below here

            // Step 1
            // Attempt to initialize workspace
            try {
                WorkspaceFileHandler.initializeWorkspace();
            } catch (IOException e) {
                System.err.println("Failed to initialize workspace at path \""
                + ((Path) AppConfig.WORKSPACE.getValue()).toAbsolutePath().normalize().toString() + "\"");
                e.printStackTrace(System.err);
                return;
            }

        } else if (!lastUsedVersion.equals(VERSION)) {
            // Any steps for updating LogiSpim's persistent files go below here.

            // This would primarily handle migrating config fields across versions, but no incompatibilities across
            // versions exist yet.

            String[] pastVersions = new String[]{
                    "0.0.1-alpha",
                    "0.1.0-beta"
            };

            try {
                // Initialize the VERSION config value with the current version
                AppConfig.VERSION.setValue(VERSION);
            } catch (AppConfigException e) {
                // VERSION is always of type String (as is required), so no exceptions are ever thrown
            }
        }

        // Creates workspace directory (with its src & out subdirectories) if they do not exist.
        // Writes the Logisim-evolution .circ file if the one for this version is missing.
        try {
            WorkspaceFileHandler.patchWorkspace();
        } catch (IOException e) {
                System.err.println("Failed to patch workspace at path \""
                + ((Path) AppConfig.WORKSPACE.getValue()).toAbsolutePath().normalize().toString() + "\"");
                e.printStackTrace(System.err);
        }

        App app = new App("LogiSpim");
    }

    private static void validateBinutilsConfig() throws ConfigurationException {
        // Ensures binutils directory ENV variable is set
        String binutilsDir = System.getProperty("logispim.binutils.dir");

        // Handles when logispim.binutils.dir ENV variable is not set (app is not bundled)
        if (binutilsDir == null) {
            System.err.println(
                    "Your run configuration does not set the ENV variable logispim.binutils.dir.\n" +
                            "For more information, see LogiSpim/Application/binutils/README.md.\n"
            );

            Path currentWorkingDirectory = Path.of("").toAbsolutePath();

            // Assume launched from LogiSpim project directory
            Path binutilsDirAssumingProjectRoot = currentWorkingDirectory.resolve(
                    "Application",
                    "binutils",
                    "target",
                    "binutils"
            ).toAbsolutePath().normalize();

            // Assume launched from Application module root directory
            Path binutilsDirAssumingApplicationRoot = currentWorkingDirectory.resolve(
                    "binutils",
                    "target",
                    "binutils"
            ).toAbsolutePath().normalize();

            if (binutilsDirAssumingProjectRoot.toFile().isDirectory()) {
                System.err.println("Defaulting to: " + binutilsDirAssumingProjectRoot + "\n");

                System.setProperty("logispim.binutils.dir", binutilsDirAssumingProjectRoot.toString());

            } else if (binutilsDirAssumingApplicationRoot.toFile().isDirectory()) {
                System.err.println("\nDefaulting to: " + binutilsDirAssumingApplicationRoot);

                System.setProperty("logispim.binutils.dir", binutilsDirAssumingApplicationRoot.toString());
            } else {

                System.err.println();
                throw new ConfigurationException(
                        "Follow the steps in the README.md listed above to build a local instance of the GNU binutils" +
                                "for cross-compiling MIPS code."
                );
            }
        }
    }
}
