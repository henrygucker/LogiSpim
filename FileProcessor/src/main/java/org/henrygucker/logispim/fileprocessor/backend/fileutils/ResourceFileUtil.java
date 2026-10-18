package org.henrygucker.logispim.fileprocessor.backend.fileutils;

import org.henrygucker.logispim.fileprocessor.backend.WorkspaceFileHandler;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class ResourceFileUtil {

    /**
     * Copies contents of a resource into a file at a given path.
     * @param resourceName The name of the resource to copy
     * @param destination A {@link Path} to the desired file to be created (overwrites existing file)
     * @return The {@link Path} to the resulting file on success, {@code null} on failure.
     * @throws IOException if an IO issue occurs when accessing the resource or copying the resource.
     */
    public static Path copyResourceToLocation(String resourceName, Path destination) throws IOException {
        try (InputStream is = ClassLoader.getSystemResourceAsStream(resourceName)) {
            if (is == null)
                return null;

            Files.copy(is, destination, StandardCopyOption.REPLACE_EXISTING);
        }

        return destination;
    }

    /**
     * Writes appropriate linker script to local file to be read by linker and returns path to written file.
     * @param isKernelText {@code true} if the user is compiling kernel instructions, otherwise {@code false}.
     * @param destination {@link Path} to where the linker script will be written
     * @return {@link Path} to appropriate linker script file
     */
    public static Path writeLinkerScript(boolean isKernelText, Path destination) throws IOException {
        // Guarantee parent directory exists (should already exist)
        Files.createDirectories(destination.getParent());

        // Name of resource file to read in resources directory of source code
        String linkerScriptResourceName = "linkerscript.ld";
        if (isKernelText)
            linkerScriptResourceName = "linkerscript-kernel-text.ld";

        // Note that the output should never be null since both possible values of linkerScriptResourceName  are
        // resources of the application
        return copyResourceToLocation(linkerScriptResourceName, destination);
    }


}
