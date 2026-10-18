package org.henrygucker.logispim.fileprocessor.backend;

import org.henrygucker.logispim.fileprocessor.backend.exceptions.CompilationException;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MIPSCrossCompiler {


    private static final String[] asmArgs = {"-EL", "-g", "-msoft-float", "-O0"};
    private static final String[] ldArgs = {"-EL"};

    /**
     * Compiles the file located at the entered source path into an ELF file.
     * @param sourcePath A {@link Path} to the source MIPS assembly file containing a main function that will be called.
     * @param linkerScriptPath A {@link Path} to the linker script that will be used by the linker.
     * @param outputDirectoryPath The home directory for all outputs.
     * @param isKernelText true if {@code  sourcePath} is to be compiled to kernel instructions.
     * @return the {@link Path} to the generated ELF file.
     * @throws IOException if an issue with IO occurs.
     * @throws InterruptedException if the thread working on compiling the
     * @throws CompilationException if an error occurs with either the assembler or linker; the isolated message is the
     * output of the assembler/linker.
     */
    public static Path compile(Path sourcePath, Path linkerScriptPath, Path outputDirectoryPath, boolean isKernelText) throws IOException, InterruptedException, CompilationException {
        String sourceName = sourcePath.getFileName().toString().split("\\.")[0];


        Path thisOutputDir = outputDirectoryPath.resolve(sourceName);
        Path binDir = thisOutputDir.resolve("bin");
        Path preProcessedAsmFilePath = binDir.resolve(sourceName + "_processed.s");
        Path objectFilePath = binDir.resolve(sourceName + ".o");
        Path elfFilePath = binDir.resolve(sourceName + ".elf");
        OSDependentData commands = OSDependentData.getForCurrentOS();

        // Creates /sourceName/bin directory and all parent directories
        Files.createDirectories(binDir);

        preprocessor(sourcePath, preProcessedAsmFilePath, isKernelText);

        // Assembling
        ArrayList<String> assemblerArgs = new ArrayList<>();
        for (String c : commands.getAssemblerCommand())
            assemblerArgs.add(c);

        for (String arg : asmArgs) {
            assemblerArgs.add(arg);
        }

        assemblerArgs.add("-o");
        assemblerArgs.add(OSDependentData.pathToOSString(objectFilePath));

        // Assembler input file
        assemblerArgs.add(OSDependentData.pathToOSString(preProcessedAsmFilePath));

        ProcessBuilder pb = new ProcessBuilder(assemblerArgs);
        Process assembler = pb.start();

        // Gets assembler error messages (if any)
        StringBuilder assemblerErrorMessageBuilder = new StringBuilder();
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(assembler.getErrorStream()))) {
            String line;
            int numLines = 0;
            while ((line = bufferedReader.readLine()) != null && numLines < 15) {
                assemblerErrorMessageBuilder.append(line);
                assemblerErrorMessageBuilder.append('\n');

                numLines++;
            }

            if (line != null && numLines == 15)
                assemblerErrorMessageBuilder.append("...\n");
        }

        if (assemblerErrorMessageBuilder.length() != 0) {
            throw new CompilationException("Assembler Error", assemblerErrorMessageBuilder.toString());
        }

        assembler.waitFor(15, TimeUnit.SECONDS);

        if (assembler.isAlive()) {
            assembler.destroy();

            throw new CompilationException("Assembler Error", "Runtime Limit Exceeded.");
        }

        // Linking
        ArrayList<String> linkerArgs = new ArrayList<>();
        for (String c : commands.getLinkerCommand())
            linkerArgs.add(c);

        for (String arg : ldArgs) {
            linkerArgs.add(arg);
        }

        linkerArgs.add("-T");
        linkerArgs.add(OSDependentData.pathToOSString(linkerScriptPath));

        linkerArgs.add(OSDependentData.pathToOSString(objectFilePath));

        linkerArgs.add("-o");
        linkerArgs.add(OSDependentData.pathToOSString(elfFilePath));

        pb = new ProcessBuilder(linkerArgs);
        Process linker = pb.start();

        // Prints stdout from linker (truncates at 15 lines)
        StringBuilder linkerErrorMessageBuilder = new StringBuilder();
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(linker.getErrorStream()))) {
            String line;
            long numLines = 0;
            while ((line = bufferedReader.readLine()) != null && numLines < 15) {
                linkerErrorMessageBuilder.append(line);
                linkerErrorMessageBuilder.append('\n');

                numLines++;
            }

            if (line != null && numLines == 15)
                linkerErrorMessageBuilder.append("...\n");
        }

        if (linkerErrorMessageBuilder.length() != 0) {
            throw new CompilationException("Linker Error", linkerErrorMessageBuilder.toString());
        }

        linker.waitFor(15, TimeUnit.SECONDS);

        if (linker.isAlive()) {
            linker.destroy();

            throw new CompilationException("Assembler Error", "Runtime Limit Exceeded.");
        }

        return elfFilePath;
    }

    /**
     * Pre-processor for MIPS assembly files to be run in the LogiSpim simulator.<br>
     * <br>
     * It is expected that the program should have the text section start with a "main" label which the program will
     * jump to, and when that function returns, the program will exit.
     * @param sourcePath A {@link Path} to the source MIPS assembly file containing a main function that will be called.
     * @param destination The {@link Path} to the resulting MIPS assembly file.
     * @param isKernelText true if {@code  sourcePath} is to be compiled to kernel instructions.
     * @throws IOException if an issue with IO occurs.
     */
    private static void preprocessor(Path sourcePath, Path destination, boolean isKernelText) throws IOException {
        final String template = "_entry:\n" +
                "\tjal main\n" +
                "\n" +
                "\t# exit program\n" +
                "\tli $v0, 10\n" +
                "\tmove $k0, $ra\n" +
                "\tsyscall\n" +
                "\tnop\n" +
                "\n";

        try (
                BufferedReader bufferedReader = new BufferedReader(new FileReader(sourcePath.toFile()));
                BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(destination.toFile()))
        ) {
            String inn = "";
            while ((inn = bufferedReader.readLine()) != null) {
                boolean innContainsComment = Pattern.compile("#(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)").matcher(inn).find();

                for (String split : Arrays.stream(inn.split("#(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)")) // Splits each line at any # character not enclosed in quotes
                        .limit(1)
                        .flatMap(str -> Arrays.stream(str.split(";(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)")))
                        .flatMap(str -> splitIfLabelNotWithAssemblerDirective(str))
                        .map(String::trim)
                        .filter(str -> str.length() != 0 || !innContainsComment)
                        .collect(Collectors.toCollection(ArrayList::new))
                ) {

                    // Insert before current instruction
                    if (!isKernelText && split.equals("syscall"))
                        bufferedWriter.write("\tmove $k0, $ra # Inserted by preprocessor due to syscall\n");

                    // Skips writing line if it was entirely a comment
                    if (split.trim().length() == 0 && inn.contains("#"))
                        continue;

                    // Writing current instruction
                    if (!split.contains(":"))
                        bufferedWriter.write('\t');
                    bufferedWriter.write(split.trim());
                    bufferedWriter.write('\n');


                    // Insert after current instruction
                    if (!isKernelText && split.trim().equals(".text"))
                        bufferedWriter.write(template);
                    else if (!isKernelText && split.trim().equals("syscall"))
                        bufferedWriter.write("\tnop\n");
                }
            }
        }
    }

    /**
     * Splits a line if it is a label followed by something other than an assembler directive. Used by preprocessor.
     * @param line A {@link String} line comments removed and not containing semicolons allowing multiple instructions in the same line.
     * @return A {@link Stream} of the split {@link String} to be flattened.
     */
    private static Stream<String> splitIfLabelNotWithAssemblerDirective(String line) {
        // Splits at first occurrence of ':' that does not occur inside a string literal
        String[] split = line.split(":(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", 1);

        // Return if no split
        if (split.length < 2)
            return Stream.of(line);

        split[0] += ':';

        // Returns the line as it was if the label is followed by a directive
        boolean containsDirective = split[1].trim().startsWith(".");
        if (containsDirective) {
            return Stream.of(split[0] + split[1]);
        }

        // Returns the split line if it was not a directive following the label (splits line if instructions were
        // written on the same line as a label).
        return Stream.of(split);
    }
}
