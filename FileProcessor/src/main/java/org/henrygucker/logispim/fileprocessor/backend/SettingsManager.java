package org.henrygucker.logispim.fileprocessor.backend;

import org.henrygucker.logispim.fileprocessor.backend.exceptions.SettingsManagerException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;

public class SettingsManager {
    public enum Settings {
        OUTPUT_DIRECTORY_ABSOLUTE_PATH("output_dir_abs_path", Path.class, (String val) -> {
            try {
                return Paths.get(val);
            } catch (InvalidPathException e) {
                return null;
            }
        }, System.getProperty("user.home") + File.separator + "LogiSpim" + File.separator + "out"),
        SOURCE_FILE_ABSOLUTE_PATH("source_file_abs_path", Path.class, (String val) -> {
            try {
                return Paths.get(val);
            } catch (InvalidPathException e) {
                return null;
            }
        },System.getProperty("user.home") + File.separator + "LogiSpim"),
        IS_KERNEL_TEXT("is_kernel_text", Boolean.class, (String val) -> val.equals("true"), "false");

        private final String xmlTag;
        private final Class dataClass;

        /**
         * A {@link Function}to parse the stored {@link String} to an {@link Object} of the correct associated type.
         */
        private final Function<String, Object> castingFunc;
        private final String defaultValue;

        Settings(String xmlTag, Class dataClass, Function<String, Object> castingFunc, String defaultValue) {
           this.xmlTag = xmlTag;
           this.dataClass = dataClass;
           this.castingFunc = castingFunc;
           this.defaultValue = defaultValue;
        }

        /**
         * Gets the XML tag of this setting used in the settings file.
         * @return a {@link String} which is used to reference this setting's value in the settings xml file.
         */
        public String getXmlTag() {
            return xmlTag;
        }

        /**
         * Gets the class associated with the data type of this setting. Can be used to cast retrieved data.
         * @return the {@link Class} that defines the type of data stored under this setting.
         */
        public Class getDataClass() {
            return dataClass;
        }

        /**
         * Gets the default value of this setting.
         * @return The {@link String} representation of the default value of this setting.
         */
        public String getDefaultValue() {
            return defaultValue;
        }
    }

    private Path file;

    /**
     * Initializes a {@link SettingsManager} object, and creates the default settings file at the specified path, if a file
     * does not already exist there.
     * @param filePath The {@link Path} to the settings file.
     * @throws SettingsManagerException if an issue occurs when creating the default file.
     */
    public SettingsManager(Path filePath) throws SettingsManagerException {
        file = filePath;

        if (!filePath.toFile().exists())
            createDefaultFile(filePath);
    }


    /**
     * Gets a setting from the settings file located at this.file
     * @param setting The setting to retrieve the value of
     * @return {@link Object} of setting's value, can be converted to the type associated with the setting enum value
     *         from Settings.getDataClass(). Returns null if there was an issue with the settings file's formatting, and
     *         the file was reset.
     * @throws SettingsManagerException if an issue occurs with IO, or if an issue occurs while resetting the settings
     *         file to default values.
     */
    public synchronized Object get(Settings setting) throws SettingsManagerException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        Document doc;
        try {
            builder = factory.newDocumentBuilder();
            doc = builder.parse(file.toFile());
        } catch (ParserConfigurationException | SAXException e) {
            // Error in document format; resetting file
            createDefaultFile(file);

            return null;
        } catch (IOException e) {
            throw new SettingsManagerException("Error occurred with IO while parsing settings file. Try again. \"" + e.getMessage() + "\"");
        }

        Object out = doc.getElementsByTagName(setting.xmlTag).item(0).getTextContent();
        if (out == null) {
            // Value does not exist in file; resetting file (should eventually allow other settings to persist in later update)
            createDefaultFile(file);

            return null;
        }

        return setting.castingFunc.apply((String) out);
    }

    /**
     * Updates a value in the settings file.
     * @param setting Which {@link Settings} value to update.
     * @param value The new value to set. Will be stored and retrieved as text using the object's .toString() method.
     * @throws SettingsManagerException if an issue with parsing the file occurs, or if an issue with IO occurs.
     */
    public synchronized void set(Settings setting, Object value) throws SettingsManagerException {
        Document doc;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            doc = builder.parse(file.toFile());
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new SettingsManagerException("Unable to parse file while updating setting in settings file located at \"" + file.toString() + "\"");
        }

        Node curr = doc.getElementsByTagName(setting.getXmlTag()).item(0);
        curr.setTextContent(value.toString());

        Transformer transformer;
        try {
            transformer = TransformerFactory.newInstance().newTransformer();
        } catch (TransformerConfigurationException e) {
            throw new SettingsManagerException("Unable to fetch new transformer while updating setting in settings file located at \"" + file.toString() + "\"");
        }

        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

        try {

            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(file.toFile());
            transformer.transform(source, result);

        } catch (TransformerException e) {
            throw new SettingsManagerException("Attempt to write default settings file failed. Target path: \"" + file.toString() + "\"");
        }
    }

    /**
     * Restores the default settings values in the settings file.
     * @throws SettingsManagerException if an issue occurs with writing to the file or with a document or transformer.
     */
    public synchronized void restoreDefaults() throws SettingsManagerException {
        createDefaultFile(file);
    }

    private static synchronized void createDefaultFile(Path path) throws SettingsManagerException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
        Transformer transformer = null;
        try {
            builder = factory.newDocumentBuilder();
            transformer = TransformerFactory.newInstance().newTransformer();
        } catch (ParserConfigurationException | TransformerConfigurationException e) {
            throw new SettingsManagerException("Error while creating document builder and transformer for writing/overwriting settings file with default values.");
        }

        Document doc;
        doc = builder.newDocument();

        Node settingsNode = doc.createElement("settings");
        doc.appendChild(settingsNode);

        Node curr;
        for (Settings setting : Settings.values()) {
            curr = doc.createElement(setting.getXmlTag());

            curr.setTextContent(setting.getDefaultValue());

            settingsNode.appendChild(curr);
        }


        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

        try {

            DOMSource source = new DOMSource(doc);
            Files.createDirectories(Path.of(path.toFile().getParent()));
            StreamResult result = new StreamResult(path.toFile());
            transformer.transform(source, result);

        } catch (TransformerException e) {
            throw new SettingsManagerException("Attempt to write default settings file failed. Target path: \"" + path.toString() + "\"");
        } catch (IOException e) {
            throw new SettingsManagerException("Attempt to create settings file parent directory failed. Target path: \"" + path.toString() + "\"");
        }
    }
}
