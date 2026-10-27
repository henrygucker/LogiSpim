package org.henrygucker.logispim.fileprocessor.backend.fileutils;

import org.henrygucker.logispim.fileprocessor.App;
import org.henrygucker.logispim.fileprocessor.backend.OSDependentData;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Class storing static methods for general-purpose file utilities
 */
public class FileUtil {
    /**
     * Deletes file/directory at a specified path recursively.
     * @param path The {@link Path} to the file/directory that will be deleted. Will do nothing if no file exists at this {@link Path}.
     * @throws IOException If an IO issue occurs.
     * @throws SecurityException Thrown by the security manager to indicate a lack of permission to delete a file.
     */
    public static void deleteRecursive(Path path) throws IOException, SecurityException {
        // Skip if non-existent path
        if (!path.toFile().exists())
            return;

        // Deletes file at path
        if (!path.toFile().isDirectory()) {
            Files.delete(path);
            return;
        }

        // Deletes contents of directory
        List<Path> childrenPaths;
        try (Stream<Path> entries = Files.list(path)) {
            childrenPaths = entries.collect(Collectors.toUnmodifiableList());

            for (Path child : childrenPaths) {
                deleteRecursive(child);
            }
        } finally {
            Files.delete(path);
        }
    }

    /**
     * Searches the PATH environment variable for an executable with a given name (including extension).
     * @param name The name of the executable file (including file extensions)
     * @return A {@link Path} to the executable binary found. If no matching executable is found, returns {@code null}.
     */
    public static Path findExecutable(String name) {
        String pathEnv = OSDependentData.getPATHEnvironmentVariable();
        if (pathEnv == null) {
            return null;
        }

        String[] pathDirs = pathEnv.split(File.pathSeparator);
        for (String dir : pathDirs) {
            Path dirPath = Path.of(dir);
            File file = dirPath.resolve(name).normalize().toFile();
            if (file.isFile() && file.canExecute()) {
                return file.getAbsoluteFile().toPath().normalize();
            }
        }
        return null;
    }

    /**
     * Opens an OS-native file explorer window to the entered directory.
     * @param app The {@link App} instance used to create error messages if any errors occur.
     * @param path A {@link Path} to the desired directory to be opened
     */
    public static void openDirectory(App app, Path path) {
        // Ensure the Desktop API is supported by the current OS platform
        if (!Desktop.isDesktopSupported()) {
            app.errorMessage("Error Opening Directory", "Desktop API is not supported on this platform.");
            return;
        }

        Desktop desktop = Desktop.getDesktop();
        File dirToOpen = new File(path.toUri());

        try {
            if (dirToOpen.exists() && dirToOpen.isDirectory()) {
                // Launches the native OS file explorer
                desktop.open(dirToOpen);
            } else {
                app.errorMessage("Directory Not Found", "The directory does not exist.");
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
            app.errorMessage("Error", "Failed to open file explorer: " + e.getMessage());
            return;
        }
    }
}
