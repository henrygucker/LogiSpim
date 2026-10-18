package org.henrygucker.logispim.fileprocessor.backend.fileutils;

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
}
