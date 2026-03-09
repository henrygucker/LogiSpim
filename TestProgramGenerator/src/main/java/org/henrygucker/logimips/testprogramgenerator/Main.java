package org.henrygucker.logimips.testprogramgenerator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.stream.Stream;

public class Main {
    private static final String outputFilename = "testing_test.s";
    private static final Path outputDirectory = Paths.get(System.getProperty("user.home") + File.separator + "LogiSpim" + File.separator + "src" + File.separator + "generated_tests");

    public static void main(String[] args) {
        String preface = "Various tests...";
        ArrayList<LogiSpimTest> tests = new ArrayList<>();

        // Test Instruction Interface:
        // Primary Test Value: $s0
        // Secondary Test Value: $s2
        // Output value: $a0
        tests.add(new LogiSpimTest(
                tests.size() + 1,
                "add $a0, $s0, $s2",
                new int[]{0, Integer.MAX_VALUE, 20, -9},
                new int[]{0, Integer.MIN_VALUE, 40, 39},
                new int[]{0, -1, 60, 30}
        ));

        // Adding tests from test streams
        boolean testsAddedSuccessfully = getFunc01Tests(tests.size())
                .map((LogiSpimTest test) -> tests.add(test))
                .filter((Boolean bool) -> !bool.booleanValue())
                .count() == 0;

        testsAddedSuccessfully = testsAddedSuccessfully && getFunc03Tests(tests.size())
                .map((LogiSpimTest test) -> tests.add(test))
                .filter((Boolean bool) -> !bool.booleanValue())
                .count() == 0;

        testsAddedSuccessfully = testsAddedSuccessfully && getFunc04Tests(tests.size())
                .map((LogiSpimTest test) -> tests.add(test))
                .filter((Boolean bool) -> !bool.booleanValue())
                .count() == 0;

        testsAddedSuccessfully = testsAddedSuccessfully && getFunc05Tests(tests.size())
                .map((LogiSpimTest test) -> tests.add(test))
                .filter((Boolean bool) -> !bool.booleanValue())
                .count() == 0;

        testsAddedSuccessfully = testsAddedSuccessfully && getFunc07Tests(tests.size())
                .map((LogiSpimTest test) -> tests.add(test))
                .filter((Boolean bool) -> !bool.booleanValue())
                .count() == 0;

        testsAddedSuccessfully = testsAddedSuccessfully && getFunc08Tests(tests.size())
                .map((LogiSpimTest test) -> tests.add(test))
                .filter((Boolean bool) -> !bool.booleanValue())
                .count() == 0;

        if (generateTestFile(preface, tests))
            System.out.println("Test program successfully created at \"" + outputDirectory.resolve(outputFilename) + "\".");
    }
    
    static boolean generateTestFile(String preface, ArrayList<LogiSpimTest> tests) {
        StringBuilder testData = new StringBuilder();
        for (int i = 0; i < tests.size() - 1; i++) {
            testData.append(tests.get(i).getTestContentDataEntry());
            testData.append('\n');
        }
        testData.append(tests.get(tests.size() - 1).getTestContentDataEntry());


        StringBuilder instructionStringDeclarations = new StringBuilder();
        for (int i = 0; i < tests.size() - 1; i++) {
            instructionStringDeclarations.append(tests.get(i).getTestInstructionStringDataEntry());
            instructionStringDeclarations.append('\n');
        }
        instructionStringDeclarations.append(tests.get(tests.size() - 1).getTestInstructionStringDataEntry());


        StringBuilder testJumpInstructions = new StringBuilder();
        for (int i = 0; i < tests.size() - 1; i++) {
            testJumpInstructions.append(tests.get(i).getTestJumpEntry());
            testJumpInstructions.append('\n');
        }
        testJumpInstructions.append(tests.get(tests.size() - 1).getTestJumpEntry());

        // Building Main Boilerplate
        String mainBoilerplate = BoilerplateGenerator.MAIN_BOILERPLATE.getContent(new String[]{
                testData.toString(),
                instructionStringDeclarations.toString(),
                preface,
                testJumpInstructions.toString()
        });
        if (mainBoilerplate == null)
            return false;


        // Building Test Functions
        StringBuilder testFunctions = new StringBuilder();

        String[] arguments = {"null", "null"};
        String curr = null;

        for (LogiSpimTest test : tests) {
            arguments = new String[]{test.getNumString(), test.getTestInstruction()};
            curr = BoilerplateGenerator.TEST_BOILERPLATE.getContent(arguments);

            // Error occurred in getContent()
            if (curr == null) {
                return false;
            }

            testFunctions.append(curr);
        }

        // Writing to output file
        try {
            Files.createDirectories(outputDirectory);
        } catch (IOException e) {
            System.err.println("There was an issue creating the output directory.");
            return false;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputDirectory.resolve(outputFilename).toFile()))) {

            writer.write(mainBoilerplate.toString());
            writer.write(testFunctions.toString());

        } catch (IOException e) {
            System.err.println("An issue occurred with IO while writing file.");
            return false;
        }
        
        return true;
    }

    static Stream<LogiSpimTest> getFunc01Tests(int currentNumTests) {
        return Stream.of(new LogiSpimTest[]{
                new LogiSpimTest(
                        ++currentNumTests,
                        "sll $a0, $s0, 4",
                        new int[]{1, 0, 0x80000000},
                        null,
                        new int[]{16, 0, 0}
                ),
                new LogiSpimTest(
                        ++currentNumTests,
                        "sll $a0, $s0, 0",
                        new int[]{1, 0, 0x80000000},
                        null,
                        new int[]{1, 0, 0x80000000}
                ),
                new LogiSpimTest(
                        ++currentNumTests,
                        "sll $a0, $s0, 31",
                        new int[]{1, 0, 0x80000000},
                        null,
                        new int[]{0x80000000, 0, 0}
                )
        });
    }

    // No Func 02

    static Stream<LogiSpimTest> getFunc03Tests(int currentNumTests) {
        return Stream.of(new LogiSpimTest[]{
                new LogiSpimTest(
                        ++currentNumTests,
                        "srl $a0, $s0, 4",
                        new int[]{0x10000, 0, 0x80000000},
                        null,
                        new int[]{0x1000, 0, 0x08000000}
                ),
                new LogiSpimTest(
                        ++currentNumTests,
                        "srl $a0, $s0, 0",
                        new int[]{1, 0, 0x80000000},
                        null,
                        new int[]{1, 0, 0x80000000}
                ),
                new LogiSpimTest(
                        ++currentNumTests,
                        "srl $a0, $s0, 31",
                        new int[]{1, 0, 0x80000000},
                        null,
                        new int[]{0, 0, 1}
                )
        });
    }

    static Stream<LogiSpimTest> getFunc04Tests(int currentNumTests) {
        return Stream.of(new LogiSpimTest[]{
                new LogiSpimTest(
                        ++currentNumTests,
                        "sra $a0, $s0, 4",
                        new int[]{0x10000, 0, 0x80000000},
                        null,
                        new int[]{0x1000, 0, 0xf8000000}
                ),
                new LogiSpimTest(
                        ++currentNumTests,
                        "sra $a0, $s0, 0",
                        new int[]{1, 0, 0x80000000},
                        null,
                        new int[]{1, 0, 0x80000000}
                ),
                new LogiSpimTest(
                        ++currentNumTests,
                        "sra $a0, $s0, 31",
                        new int[]{1, 0, 0x80000000},
                        null,
                        new int[]{0, 0, 0xffffffff}
                )
        });
    }

    static Stream<LogiSpimTest> getFunc05Tests(int currentNumTests) {
        return Stream.of(new LogiSpimTest[]{
                new LogiSpimTest(
                        ++currentNumTests,
                        "sllv $a0, $s0, $s2",
                        new int[]{0x1, 0x80000000, 0x80000000, 0x12345678},
                        new int[]{4, 4, 4, 0},
                        new int[]{0x10, 0, 0, 0x12345678}
                )
        });
    }

    // No Func 06

    static Stream<LogiSpimTest> getFunc07Tests(int currentNumTests) {
        return Stream.of(new LogiSpimTest[]{
                new LogiSpimTest(
                        ++currentNumTests,
                        "srlv $a0, $s0, $s2",
                        new int[]{0x10, 0x80000000, 0x80000000, 0x12345678},
                        new int[]{4, 31, 4, 0},
                        new int[]{0x1, 1, 0x08000000, 0x12345678}
                )
        });
    }

    static Stream<LogiSpimTest> getFunc08Tests(int currentNumTests) {
        return Stream.of(new LogiSpimTest[]{
                new LogiSpimTest(
                        ++currentNumTests,
                        "srav $a0, $s0, $s2",
                        new int[]{0x10, 0x80000000, 0x80000000, 0x12345678},
                        new int[]{4, 31, 4, 0},
                        new int[]{0x1, 0xffffffff, 0xf8000000, 0x12345678}
                )
        });
    }

    static Stream<LogiSpimTest> getFunc09Tests(int currentNumTests) {
        // jr
        return Stream.of(new LogiSpimTest[]{
                new LogiSpimTest(
                        ++currentNumTests,
                        "li $a0, 0\n" +
                                "la $t0, test_" + currentNumTests + "_jump_testing_addr\n" +
                                "add $s0, $t0, $s0\n" +
                                "jr $s0\n" +
                                "j test_" + currentNumTests + "_jump_test_skip_addr:\n" +

                                "test_" + currentNumTests + "_jump_testing_addr:\n" +
                                "andi $a0, $a0, 2\n" +
                                "andi $a0, $a0, 1\n" +

                                "test_" + currentNumTests + "_jump_test_skip_addr:\n",

                        new int[]{0, 4, 8}, // Offsets on value in register to jump to
                        null,
                        new int[]{3, 1, 0}
                )
        });
    }
}