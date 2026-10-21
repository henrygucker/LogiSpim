package org.henrygucker.logispim.fileprocessor.backend;

import org.henrygucker.logispim.fileprocessor.backend.exceptions.AppConfigException;
import org.henrygucker.logispim.fileprocessor.backend.fileutils.ResourceFileUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Class with static methods for managing the LogiSpim workspace directory
 */
public class WorkspaceFileHandler {

    /**
     * Migrate the current workspace directory to a specified directory. Assumes that the workspace directory stored
     * by {@link AppConfig} is <b>NOT</b> the new directory.
     * @param newDir The {@link Path} to the new workspace directory. This directory does not necessarily have to exist.
     */
    public static void migrateWorkspace(Path newDir) {
        Path oldDir = (Path) AppConfig.WORKSPACE.getValue();

        try {
            Files.move(oldDir, newDir, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            // Occurs if new and old workspace directories are on different memory store partitions

            try {
                AppConfig.WORKSPACE.setValue(newDir);
                initializeWorkspace();
            } catch (IOException f) {
                System.err.println("Invalid workspace directory selected. Please select a new workspace directory.");
            } catch (AppConfigException _) {} // Cannot be thrown since newDir is a Path
        }

        // Updating source path moved file if the last chosen src file was in the old workspace
        Path oldSourcePath = (Path) AppConfig.SOURCE_FILE.getValue();

        oldSourcePath = oldSourcePath.toAbsolutePath().normalize();
        oldDir = oldDir.toAbsolutePath().normalize();

        Path newSourcePath = null;
        if (oldSourcePath.startsWith(oldDir)) {
            Path relativeToSrc = oldDir.relativize(oldSourcePath);
            newSourcePath = newDir.resolve(relativeToSrc);
        } else {
            newSourcePath = oldSourcePath;
        }

        try {
            AppConfig.WORKSPACE.setValue(newDir);
            AppConfig.SOURCE_FILE.setValue(newSourcePath);
        } catch (AppConfigException _) {} // Never throws an AppConfigException since params are of type Path
    }

    /**
     * Gets the workspace output directory.
     * @return The {@link Path} to the workspace output directory
     */
    public static Path getOutputDir() {
        return ((Path) AppConfig.WORKSPACE.getValue()).resolve("out");
    }

    /**
     * Gets the default directory for source files.
     * @return The {@link Path} to the workspace's default directory for source files
     */
    public static Path getDefaultSourceDir() {
        return ((Path) AppConfig.WORKSPACE.getValue()).resolve("src");
    }

    /**
     * Initializes the workspace directory at the currently stored workspace directory.
     * Does not require a directory to exist at the specified directory.
     * @throws IOException if an issue occurs with IO, possibly a lack of permissions
     */
    public static void initializeWorkspace() throws IOException {
        // Each workspace has the following files and directories:
        // oldDir/
        // oldDir/src (for MIPS source code)
        // oldDir/src/hello_world.s (example hello world program)
        // oldDir/out (for compiled output directories named after the source file they were compiled from)
        // oldDir/LogiSpim.circ (Logisim-evolution circuit for this version of LogiSpim)

        // Create ./src and ./src/hello_world.s
        Files.createDirectories(getDefaultSourceDir());
        ResourceFileUtil.copyResourceToLocation("hello_world.s", getDefaultSourceDir().resolve("hello_world.s"));

        // Create ./out
        Files.createDirectories(getOutputDir());

        // Create ./LogiSpim.circ
        if (null == ResourceFileUtil.copyResourceToLocation("LogiSpim.circ", ((Path) AppConfig.WORKSPACE.getValue()).resolve("LogiSpim.circ")))
            System.err.println("Failed to copy instance of Logisim-evolution circuit file when initializing workspace.");
    }

    /**
     * Repairs a workspace, ensuring that it contains a source directory, output directory, and copy of the Logisim circuit file
     * @throws IOException if an IO issue occurs when modifying files, possibly a lack of permissions
     */
    public static void repairWorkspace() throws IOException {
        // this will later earn its own button for users to repair their workspace

        // Create source and output directories
        Files.createDirectories(getOutputDir());
        Files.createDirectories(getDefaultSourceDir());

        if (null == ResourceFileUtil.copyResourceToLocation("LogiSpim.circ", ((Path) AppConfig.WORKSPACE.getValue()).resolve("LogiSpim.circ")))
            System.err.println("Failed to copy new instance of Logisim-evolution circuit file when repairing workspace.");
    }
}
