package org.henrygucker.logimips.fileprocessor.backend;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;

public enum PlatformToolchainCommands {
    MACOS("MacOS", new String[]{
            "mipsel-linux-gnu-as"
    }, new String[]{
            "mipsel-linux-gnu-ld"
    }, new String[]{
            "mipsel-linux-gnu-objdump"
    }),
    WINDOWS("Windows", new String[]{
            "wsl", "mipsel-linux-gnu-as"
    }, new String[]{
            "wsl", "mipsel-linux-gnu-ld"
    }, new String[]{
            "wsl", "mipsel-linux-gnu-objdump"
    }),
    LINUX_DEBIAN("Linux (Debian/Ubuntu)", new String[]{
            "mipsel-linux-gnu-as"
    }, new String[]{
            "mipsel-linux-gnu-ld"
    }, new String[]{
            "mipsel-linux-gnu-objdump"
    });

    private final String displayName;
    private final String[] assemblerCommand;
    private final String[] linkerCommand;
    private final String[] objdumpCommand;

    PlatformToolchainCommands(String displayName, String[] assemblerCommand, String[] linkerCommand, String[] objdumpCommand) {
        this.displayName = displayName;
        this.assemblerCommand = assemblerCommand;
        this.linkerCommand = linkerCommand;
        this.objdumpCommand = objdumpCommand;
    }

    /**
     * Gets the arguments to be passed to a {@link ProcessBuilder} to target the appropriate assembler binary.
     * @return {@link String}{@code []} that stores the necessary arguments.
     */
    public String[] getAssemblerCommand() {
        return assemblerCommand.clone();
    }

    /**
     * Gets the arguments to be passed to a {@link ProcessBuilder} to target the appropriate linker binary.
     * @return {@link String}{@code []} that stores the necessary arguments.
     */
    public String[] getLinkerCommand() {
        return linkerCommand.clone();
    }

    /**
     * Gets the arguments to be passed to a {@link ProcessBuilder} to target the appropriate objdump binary.
     * @return {@link String}{@code []} that stores the necessary arguments.
     */
    public String[] getObjdumpCommand() {
        return objdumpCommand.clone();
    }

    /**
     * Gets the correct {@link PlatformToolchainCommands} enum value for the current operating system.
     * @return the appropriate {@link PlatformToolchainCommands} value. {@code null} if unsupported OS.
     */
    public static PlatformToolchainCommands getAppropriateCommands() {
        String osName = System.getProperty("os.name");
        String osVersion = System.getProperty("os.version");
        if (osName.startsWith("Windows")) {
            return PlatformToolchainCommands.WINDOWS;
        } else if (osName.equalsIgnoreCase("macos") || osName.equalsIgnoreCase("mac os x")) {
            return PlatformToolchainCommands.MACOS;
        } else if (isDebianBased()) {
            return PlatformToolchainCommands.LINUX_DEBIAN;
        }

        return null;
    }

    /**
     * Gets the {@link String} that should be used in the {@link ProcessBuilder} arguments to refer to a file from its {@link Path}.
     * @param path The {@link Path} directed at the file's location.
     * @return The {@link String} that accounts for being run on Windows through wsl.
     */
    public static String pathToAppropriateString(Path path) {
        if (!System.getProperty("os.name").startsWith("Windows")) {
            return path.toString();
        }

        return "\"$(wslpath '" + path.toAbsolutePath().toString() + "')\"";
    }

    /**
     * Determines if the current operating system is a linux operating system based on Debian (includes Ubuntu)
     * @return {@code true} if the current OS is some form of Debian-based OS, notably including Ubuntu, otherwise {@code false}.
     */
    private static boolean isDebianBased() {
        if (!System.getProperty("os.name").equalsIgnoreCase("linux")) {
            return false;
        }

        String id = "";
        String idLike = "";
        try (BufferedReader br = new BufferedReader(new FileReader("/etc/os-release"))) {
            String inn;
            String[] splits;
            while ((inn = br.readLine()) != null) {
                if (inn.trim().isEmpty() || !inn.contains("="))
                    continue;

                splits = inn.trim().split("=", 2);

                if (splits[0].equals("ID"))
                    id = splits[1].trim().replace("\"", "");

                if (splits[0].equals("ID_LIKE"))
                    idLike = splits[1].trim().replace("\"", "");
            }
        } catch (IOException e) {
            System.err.println("IO Issue while reading /etc/os-release file to determine Linux distribution.");
            e.printStackTrace();

            return false;
        }

        if (id.equals("debian") || id.equals("ubuntu"))
            return true;

        if (idLike.contains("debian"))
            return true;

        return false;
    }
}
