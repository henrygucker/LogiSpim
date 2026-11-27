import org.henrygucker.logispim.application.backend.OSDependentData;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class Util {
    /**
     * Searches the PATH environment variable for an executable with a given name (including extension).
     * @param name The name of the executable file (including file extensions)
     * @return A {@link Path} to the executable binary found. If no matching executable is found, returns {@code null}.
     */
    public static Path findExecutable(String name) {
        String pathEnv = getPATHEnvironmentVariable();
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

    public static String getPATHEnvironmentVariable() {
        OSDependentData currentOS = OSDependentData.getForCurrentOS();
        if (currentOS != OSDependentData.MACOS) {
            return System.getenv("PATH");
        }

        // Echos zsh's $PATH environment variable
        ProcessBuilder pb = new ProcessBuilder("/bin/zsh", "-ilc", "echo $PATH");
        Process zsh = null;
        String PATH = null;
        try {
            zsh = pb.start();
            PATH = new String(zsh.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("Failed to get shell $PATH:");
            e.printStackTrace();
        }

        return PATH;
    }
}
