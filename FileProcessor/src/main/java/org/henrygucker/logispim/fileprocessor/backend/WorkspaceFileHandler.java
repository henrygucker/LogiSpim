package org.henrygucker.logispim.fileprocessor.backend;

import org.henrygucker.logispim.fileprocessor.backend.exceptions.AppConfigException;
import org.henrygucker.logispim.fileprocessor.backend.fileutils.ResourceFileUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class WorkspaceFileHandler {

    public static void migrateWorkspace(Path newDir) {
        Path oldDir = (Path) AppConfig.WORKSPACE_DIRECTORY.getValue();

        try {
            Files.move(oldDir, newDir, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            // Occurs if new and old workspace directories are on different memory store partitions

            try {
                AppConfig.WORKSPACE_DIRECTORY.setValue(newDir);
                initializeWorkspace();
            } catch (IOException f) {
                System.err.println("Invalid workspace directory selected. Please select a new workspace directory.");
            } catch (AppConfigException _) {} // Cannot be thrown since newDir is a Path
        }

        try {
            AppConfig.WORKSPACE_DIRECTORY.setValue(newDir);
            AppConfig.LAST_CHOSEN_SOURCE_FILE_PATH.setValue(newDir.resolve("src", "hello_world.s"));
        } catch (AppConfigException _) {} // Never throws an AppConfigException since params are of type Path
    }

    public static Path getOutputDir() {
        return ((Path) AppConfig.WORKSPACE_DIRECTORY.getValue()).resolve("out");
    }

    public static Path getDefaultSourceDir() {
        return ((Path) AppConfig.WORKSPACE_DIRECTORY.getValue()).resolve("src");
    }

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
        if (null == ResourceFileUtil.copyResourceToLocation("LogiSpim.circ", ((Path) AppConfig.WORKSPACE_DIRECTORY.getValue()).resolve("LogiSpim.circ")))
            System.err.println("Failed to copy instance of Logisim-evolution circuit file when initializing workspace.");
    }

    /**
     * Repairs a workspace, ensuring that it contains a source directory, output directory, and copy of the Logisim circuit file
     * @throws IOException if an IO issue occurs when modifying files
     */
    public static void repairWorkspace() throws IOException {
        // this will later earn its own button for users to repair their workspace

        // Create source and output directories
        Files.createDirectories(getOutputDir());
        Files.createDirectories(getDefaultSourceDir());

        if (null == ResourceFileUtil.copyResourceToLocation("LogiSpim.circ", ((Path) AppConfig.WORKSPACE_DIRECTORY.getValue()).resolve("LogiSpim.circ")))
            System.err.println("Failed to copy new instance of Logisim-evolution circuit file when repairing workspace.");
    }
}
