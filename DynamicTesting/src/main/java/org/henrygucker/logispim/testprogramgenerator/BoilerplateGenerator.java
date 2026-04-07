package org.henrygucker.logispim.testprogramgenerator;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;

public enum BoilerplateGenerator {
    MAIN_BOILERPLATE("main_boilerplate.dat", new String[]{
            "{{TEST_CONTENT_INSERTION_POINT}}",
            "{{INSTRUCTION_STRING_INSERTION_POINT}}",
            "{{PREFACE}}",
            "{{TEST_JUMPS_INSERTION_POINT}}"
    }),
    TEST_BOILERPLATE("test_boilerplate.dat", new String[]{
            "{{NUM}}",
            "{{TEST_OPERATION_INSERTION_POINT}}"
    });

    private String filename;
    private String[] replacementVariables;
    BoilerplateGenerator(String filename, String[] replacementVariables) {
        this.filename = filename;
        this.replacementVariables = replacementVariables;
    }

    public String getContent(String[] replacementValues) {
        if (this.replacementVariables == null || this.replacementVariables.length != replacementValues.length) {
            System.err.println("Invalid amount of replacement values provided.");
            System.err.println("Required: " + this.replacementVariables.length);
            System.err.println("Provided: " + replacementValues.length);
            return null;
        }

        String content = "";
        try (BufferedInputStream reader = new BufferedInputStream(ClassLoader.getSystemResourceAsStream(filename))) {
            byte[] bytes = reader.readAllBytes();
            content = new String(bytes, StandardCharsets.US_ASCII);
        } catch (IOException e) {
            System.err.println("An error occurred while reading content of internal file.");
            return null;
        }

        for (int i = 0; i < this.replacementVariables.length; i++) {
            content = content.replace(this.replacementVariables[i], replacementValues[i]);
        }

        return content;
    }
}
