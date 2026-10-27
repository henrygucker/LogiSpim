package org.henrygucker.logispim.fileprocessor.backend;

import org.henrygucker.logispim.fileprocessor.backend.exceptions.AppConfigException;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * Enum which manages persistent application configuration for each of its enum values through member functions of
 * its instances
 */
public enum AppConfig {
    /**
     * Stores the last used LogiSpim version. Stores "null" if uninitialized (by default).
     * This value is to be updated once all local (and config) changes are made to support the current version.
     */
    VERSION(
            "logispim_version",
            String.class,
            (String val) -> val,
            "null"
    ),
    /**
     * Stores the normalized absolute path to the workspace directory.
     */
    WORKSPACE(
            "workspace_dir",
            Path.class,
            (String val) -> {
                try {
                    return Paths.get(val);
                } catch (InvalidPathException e) {
                    return null;
                }
            },
            OSDependentData.getForCurrentOS().getDefaultWorkspaceDir().toString()
    ),
    /**
     * Stores the normalized absolute path to the last source file chosen by the user.
     */
    SOURCE_FILE(
            "source_file_abs_path",
            Path.class,
            (String val) -> {
                try {
                    return Paths.get(val);
                } catch (InvalidPathException e) {
                    return null;
                }
            },
            OSDependentData.getForCurrentOS().getDefaultWorkspaceDir().resolve("src").resolve("hello_world.s").toString()
    ),
    /**
     * If on macOS or Linux, this stores the normalized absolute path to the directory holding the binary executables
     * for the third-party MIPS cross-compilation toolchain.
     * On Windows, this value stores the directory holding the wsl executable.
     *
     * Hence, this stored {@link Path} can be resolved with the first item of each argument array given by
     * {@link OSDependentData} to find the fully-qualified path to the desired executable for {@link ProcessBuilder}
     * to run when using any cross-compilation toolchain program.
     *
     * This setting may return the value {@code null} if a {@link Path} cannot be found.
     */
    EXTERNAL_BIN_PREFIX_DIR(
            "external_bin_prefix_dir",
            Path.class,
            (String val) -> {
                try {
                    return Paths.get(val);
                } catch (InvalidPathException e) {
                    return "";
                }
            },
            OSDependentData.getForCurrentOS().findDefaultExternalBinaryDir()
    ),
    /**
     * Value corresponding to the Compile as Kernel check box, indicating if compiler should target kernel program
     * space rather than user-space.
     */
    COMPILE_KERNEL(
            "compile_kernel",
            Boolean.class,
            (String val) -> val.equals("true"),
            "false"
    );

    private final String configKey;
    private final Class dataClass;

    private final Function<String, Object> castingFunc;
    private final String defaultValue;

    private static final Preferences PREFS = Preferences.userNodeForPackage(AppConfig.class);

    /**
     * Instantiate an enum value for a config setting
     * @param configKey The unique {@link String} used to identify this config setting
     * @param dataClass The {@link Class} of values stored by this config
     * @param castingFunc A {@link Function}to parse the stored {@link String} to an {@link Object} of the
     *                    correct associated type. Allows users to simply cast a returned {@link Object} to
     *                    the expected type of any given setting rather than repeatedly parsing returned strings.
     * @param defaultValue The default value of this config value
     */
    AppConfig(String configKey, Class dataClass, Function<String, Object> castingFunc, String defaultValue) {
        this.configKey = configKey;
        this.dataClass = dataClass;
        this.castingFunc = castingFunc;
        this.defaultValue = defaultValue;
    }

    /**
     * Gets the key associated with this config as used by {@code PREFS}.
     *
     * @return a {@link String} which is used to reference this setting's value in the settings XML file.
     */
    private String getKey() {
        return configKey;
    }

    /**
     * Gets the class associated with the data type of this setting. Can be used to cast retrieved data.
     *
     * @return the {@link Class} that defines the type of data stored under this setting.
     */
    public Class getDataClass() {
        return dataClass;
    }

    /**
     * Gets the default value of this setting.
     *
     * @return The {@link String} representation of the default value of this setting.
     */
    public String getDefaultValue() {
        return defaultValue;
    }


    /**
     * Gets the value stored for this setting
     *
     * @return {@link Object} of setting's value, can be converted to the type associated with the setting enum value
     * from Settings.getDataClass(). Returns the setting's default value if the stored value cannot be accessed.
     */
    public synchronized Object getValue() {
        return this.castingFunc.apply(
                // .toString() to clone String output and prevent accidental sharing of cached resource
                PREFS.get(this.getKey(), this.getDefaultValue()).toString()
        );
    }

    /**
     * Updates this setting's value.
     *
     * @param value The new value to set.
     * @throws AppConfigException if the type of {@code value} is not the required type of this {@link AppConfig}.
     */
    public synchronized <T extends Object> void setValue(T value) throws AppConfigException {
        // Ensure type matches
        if (!this.dataClass.isInstance(value))
            throw new AppConfigException("Value of type " + value.getClass().toString() + " does not meet the " +
                    "requirement of setting " + this.getKey() + ".");

        PREFS.put(this.getKey(), value.toString());

        // Attempt to update backing storage (local file) with updated preferences
        try {
            PREFS.flush();
        } catch (BackingStoreException e) {
            System.err.println("Failure to update AppConfig backing storage.");
            e.printStackTrace(System.err);
        }
    }

    /**
     * Resets this config value to its default value
     */
    public synchronized void restoreDefault() {
        // Cannot throw exception since type of default value matches type of expected value
        try {
            this.setValue(this.getDefaultValue());
        } catch (AppConfigException _) {}
    }

    /**
     * Clears all config values
     */
    public synchronized static void clearAll() {
        try {
            // Removes all key-value associations in this preference node
            PREFS.clear();
            // Forces changes to the persistent backing store
            PREFS.flush();
        } catch (BackingStoreException e) {
            e.printStackTrace();
        }
    }
}
