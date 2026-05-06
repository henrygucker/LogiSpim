import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class TestVectors {
    private static final String LOGISIM_EVOLUTION_VERSION = "4.1.0";
    private static final String LOGISIM_EVOLUTION_URL = "https://github.com/logisim-evolution/logisim-evolution/releases/download/v" + LOGISIM_EVOLUTION_VERSION + "/logisim-evolution-" + LOGISIM_EVOLUTION_VERSION + "-all.jar";

    static final Path logisimCircuitFilepath = Paths.get(System.getProperty("user.dir")).getParent().resolve("LogiSpim.circ");

    @TempDir(cleanup = CleanupMode.ALWAYS)
    static Path tempDir;
    static Path logisimEvolutionFilepath;

    @BeforeAll
    static void init() throws IOException, InterruptedException {
        logisimEvolutionFilepath = tempDir.resolve("logisim-evolution");

        downloadLogisim();
    }

    @Test
    void mainDecoderTestVector() throws IOException, InterruptedException {
        Path testVectorPath = tempDir.resolve("MainDecoder-test-vector.txt");
        MainDecoderMaster.writeTestVector(testVectorPath);


        runTestVector(logisimEvolutionFilepath, "MainDecoder", testVectorPath, logisimCircuitFilepath);
    }

    static void runTestVector(Path logisimJar, String componentName, Path testVector, Path circuit) throws IOException, InterruptedException {
        // java -jar logisim-evolution.jar --test-vector <circuit_name> <test_vector_file> <project.circ>
        ArrayList<String> args = new ArrayList<>();
        args.add("java");
        args.add("-jar");
        args.add(logisimJar.toAbsolutePath().toString());
        args.add("--test-vector");
        args.add(componentName);
        args.add(testVector.toAbsolutePath().toString());
        args.add(circuit.toAbsolutePath().toString());

        ProcessBuilder processBuilder = new ProcessBuilder(args);
        Process testVectorProcess = processBuilder.start();


        // Stores stdout from test vector execution
        StringBuilder outputMessage = new StringBuilder();
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(testVectorProcess.getInputStream()))) {
            String line;
            int lineCount = 0;
            while ((line = bufferedReader.readLine()) != null) {
                outputMessage.append(line);
                outputMessage.append('\n');
                lineCount++;
            }
        }

        testVectorProcess.waitFor(15, TimeUnit.SECONDS);

        Assertions.assertFalse(testVectorProcess.isAlive());

        System.out.println(outputMessage);

        // Last line of output is formatted as follows:
        // "Passed: <num passed>, Failed: <num failed>"
        String[] outputMessageLines = outputMessage.toString().split("\n");

        System.out.println("\nTest Vector Output:");
        System.out.println(outputMessage);

        String numFailed = outputMessageLines[outputMessageLines.length - 1].split("\\s+")[3];

        Assertions.assertEquals("0", numFailed);
    }

    static void downloadLogisim() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS) // Required for GitHub downloads
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(LOGISIM_EVOLUTION_URL))
                .build();

        // Downloads the file directly to the specified path
        client.send(request, HttpResponse.BodyHandlers.ofFile(logisimEvolutionFilepath));
    }
}
