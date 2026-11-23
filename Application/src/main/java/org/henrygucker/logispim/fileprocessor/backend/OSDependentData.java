package org.henrygucker.logispim.fileprocessor.backend;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;


/**
 * Enum which handles processes that vary across operating systems
 */
public enum OSDependentData {

    /**
     * Used to access data applicable to macOS
     */
    MACOS("macOS",
            "mipsel-none-elf-as",
            "mipsel-none-elf-ld",
            "mipsel-none-elf-objdump"
    ),
    /**
     * Used to access data applicable to Windows
     */
    WINDOWS("Windows",
            "mipsel-none-elf-as.exe",
            "mipsel-none-elf-ld.exe",
            "mipsel-linux-elf-objdump.exe"
    ),
    /**
     * Used to access data applicable to Debian-based Linux
     */
    LINUX("Linux",
            "mipsel-none-elf-as",
            "mipsel-none-elf-ld",
            "mipsel-none-elf-objdump"
    );

    private static final String APP_NAME = "LogiSpim";
    private static final Path BINUTILS_BIN_DIR = Path.of(System.getProperty("logispim.binutils.dir")).resolve("bin");

    private final String displayName;
    private final String assemblerCommand;
    private final String linkerCommand;
    private final String objdumpCommand;

    OSDependentData(String displayName, String assemblerCommand, String linkerCommand, String objdumpCommand) {
        this.displayName = displayName;
        this.assemblerCommand = assemblerCommand;
        this.linkerCommand = linkerCommand;
        this.objdumpCommand = objdumpCommand;
    }

    /**
     * Gets the {@link Path} of the appropriate assembler binary.
     * @return {@link Path} of the assembler binary
     */
    public Path getAssemblerPath() {
        return BINUTILS_BIN_DIR.resolve(assemblerCommand);
    }

    /**
     * Gets the {@link Path} of the appropriate linker binary.
     * @return {@link Path} of the linker binary
     */
    public Path getLinkerPath() {
        return BINUTILS_BIN_DIR.resolve(linkerCommand);
    }

    /**
     * Gets the {@link Path} of the appropriate objdump binary.
     * @return {@link Path} of the objdump binary
     */
    public Path getObjdumpPath() {
        return BINUTILS_BIN_DIR.resolve(objdumpCommand);
    }

    /**
     * Gets the default directory for the LogiSpim workspace directory
     * @return A {@link Path} to such a workspace directory
     */
    public Path getDefaultWorkspaceDir() {
        switch (this) {
            case MACOS:
            case WINDOWS:
                return Path.of(System.getProperty("user.home"), "Documents", APP_NAME);
            case LINUX:
                return Path.of(System.getProperty("user.home"), APP_NAME);
        }
        return null;
    }

    /**
     * Gets the correct {@link OSDependentData} enum value for the current operating system.
     * @return the appropriate {@link OSDependentData} value. {@code null} if unsupported OS.
     */
    public static OSDependentData getForCurrentOS() {
        String osName = System.getProperty("os.name");

        if (osName.startsWith("Windows")) {
            return OSDependentData.WINDOWS;
        } else if (osName.equalsIgnoreCase("macos") || osName.equalsIgnoreCase("mac os x")) {
            return OSDependentData.MACOS;
        } else if (osName.equalsIgnoreCase("linux")) {
            return OSDependentData.LINUX;
        }

        return null;
    }
}
