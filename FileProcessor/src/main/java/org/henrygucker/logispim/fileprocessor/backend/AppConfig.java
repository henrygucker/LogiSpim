package org.henrygucker.logispim.fileprocessor.backend;

import org.henrygucker.logispim.fileprocessor.backend.exceptions.AppConfigException;

import java.io.File;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public enum AppConfig {
    /**
     * Stores the last used LogiSpim version. Stores "N/A" if uninitialized (by default).
     * This value is to be updated once all local (and config) changes are made to support the current version.
     */
    LAST_USED_LOGISPIM_VERSION(
            "logispim_version",
            String.class,
            (String val) -> val,
            "N/A"
    ),
    /**
     * Stores the normalized absolute path to the workspace directory (selected at initialization).
     */
    WORKSPACE_DIRECTORY(
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
    LAST_CHOSEN_SOURCE_FILE_PATH(
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
    IS_KERNEL_TEXT(
            "is_kernel_text",
            Boolean.class,
            (String val) -> val.equals("true"),
            "false"
    );

    private final String xmlTag;
    private final Class dataClass;

    /**
     * A {@link Function}to parse the stored {@link String} to an {@link Object} of the correct associated type.
     */
    private final Function<String, Object> castingFunc;
    private final String defaultValue;

    private static final Preferences PREFS = Preferences.userNodeForPackage(AppConfig.class);

    AppConfig(String xmlTag, Class dataClass, Function<String, Object> castingFunc, String defaultValue) {
        this.xmlTag = xmlTag;
        this.dataClass = dataClass;
        this.castingFunc = castingFunc;
        this.defaultValue = defaultValue;
    }

    /**
     * Gets the XML tag of this setting used in the settings file.
     *
     * @return a {@link String} which is used to reference this setting's value in the settings XML file.
     */
    public String getXmlTag() {
        return xmlTag;
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
                PREFS.get(this.getXmlTag(), this.getDefaultValue()).toString() // .toString() to clone String output
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
                    "requirement of setting " + this.getXmlTag() + ".");

        PREFS.put(this.getXmlTag(), value.toString());

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

    public synchronized static void resetDefaults() {
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
