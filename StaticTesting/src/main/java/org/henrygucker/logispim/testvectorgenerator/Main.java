package org.henrygucker.logispim.testvectorgenerator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class Main {

    public static final String PROGRAM_JAR_NAME = "Tester.jar";

    private static final Path defaultOutputFilepath = Paths.get(System.getProperty("user.home") + File.separator + "LogiSpim" + File.separator + "test_vectors" + File.separator + "main_decoder_test.txt");

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.err.println("Invalid arguments.");
            System.err.println("Usage: java -jar " + PROGRAM_JAR_NAME + " <component flag> [component tester args]");
            System.err.println("Valid component flags:");
            System.err.println(MainDecoderTesting.COMPONENT_FLAG);
            assert(false);
        }

        if (args[1].equals(MainDecoderTesting.COMPONENT_FLAG)) {
            MainDecoderTesting test = new MainDecoderTesting(Arrays.copyOfRange(args, 2, args.length));
        } else {

            System.err.println("Invalid arguments.");
            System.err.println("Usage: java -jar " + PROGRAM_JAR_NAME + " <component flag> [component tester args]");
            System.err.println("Valid component flags:");
            System.err.println(MainDecoderTesting.COMPONENT_FLAG);
            assert(false);
        }
    }
}