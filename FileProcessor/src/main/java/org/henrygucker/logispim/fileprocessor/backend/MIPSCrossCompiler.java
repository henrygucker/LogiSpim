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
    private final Path sourcePath;
    private final String sourceName;

    private final Path linkerScriptPath;
    private final Path outputDirectoryPath;

    private final boolean isKernelText;


    private static final String[] asmArgs = {"-EL", "-g", "-msoft-float", "-O0"};
    private static final String[] ldArgs = {"-EL"};

    /**
     * Creates {@link MIPSCrossCompiler} object to compile the source MIPS assembly file based on the specified criteria.
     * This constructor is to be used if and only if <u>NOT</u> compiling kernel instructions.
     * @param sourcePath A {@link Path} to the source MIPS assembly file containing a main function that will be called.
     * @param linkerScriptPath A {@link Path} to the linker script that will be used by the linker.
     * @param outputDirectoryPath The home directory for all outputs.
     */
    public MIPSCrossCompiler(Path sourcePath, Path linkerScriptPath, Path outputDirectoryPath) {
        this(sourcePath, linkerScriptPath, outputDirectoryPath, false);
    }

    /**
     * Creates {@link MIPSCrossCompiler} object to compile the source MIPS assembly file based on the specified criteria.
     * @param sourcePath A {@link Path} to the source MIPS assembly file containing a main function that will be called.
     * @param linkerScriptPath A {@link Path} to the linker script that will be used by the linker.
     * @param outputDirectoryPath The home directory for all outputs.
     * @param isKernelText true if {@code  sourcePath} is to be compiled to kernel instructions.
     */
    public MIPSCrossCompiler(Path sourcePath, Path linkerScriptPath, Path outputDirectoryPath, boolean isKernelText) {
        this.sourcePath = sourcePath;
        this.sourceName = sourcePath.getFileName().toString().split("\\.")[0];

        this.linkerScriptPath = linkerScriptPath;
        this.outputDirectoryPath = outputDirectoryPath;

        this.isKernelText = isKernelText;
    }

    /**
     * Compiles the file located at the entered source path into an ELF file.
     * @return the {@link Path} to the generated ELF file.
     * @throws IOException if an issue with IO occurs.
     * @throws InterruptedException if the thread working on compiling the
     * @throws CompilationException if an error occurs with either the assembler or linker; the isolated message is the
     * output of the assembler/linker.
     */
    public Path compile() throws IOException, InterruptedException, CompilationException {
        Path thisOutputDir = outputDirectoryPath.resolve(this.sourceName);
        Path binDir = thisOutputDir.resolve("bin");
        Path preProcessedAsmFilePath = binDir.resolve(sourceName + "_processed.s");
        Path objectFilePath = binDir.resolve(sourceName + ".o");
        Path elfFilePath = binDir.resolve(sourceName + ".elf");
        PlatformToolchainCommands commands = PlatformToolchainCommands.getAppropriateCommands();

        // Creates /sourceName/bin directory and all parent directories
        Files.createDirectories(binDir);

        preprocessor(preProcessedAsmFilePath);

        // Assembling
        ArrayList<String> assemblerArgs = new ArrayList<>();
        for (String c : commands.getAssemblerCommand())
            assemblerArgs.add(c);

        for (String arg : asmArgs) {
            assemblerArgs.add(arg);
        }

        assemblerArgs.add("-o");
        assemblerArgs.add(PlatformToolchainCommands.pathToAppropriateString(objectFilePath));

        // Assembler input file
        assemblerArgs.add(PlatformToolchainCommands.pathToAppropriateString(preProcessedAsmFilePath));

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
        linkerArgs.add(PlatformToolchainCommands.pathToAppropriateString(linkerScriptPath));

        linkerArgs.add(PlatformToolchainCommands.pathToAppropriateString(objectFilePath));

        linkerArgs.add("-o");
        linkerArgs.add(PlatformToolchainCommands.pathToAppropriateString(elfFilePath));

        pb = new ProcessBuilder(linkerArgs);
        Process linker = pb.start();

        // Prints stdout from linker
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
     * @param destination The {@link Path} to the resulting MIPS assembly file.
     * @throws IOException if an issue with IO occurs.
     */
    private void preprocessor(Path destination) throws IOException {
        String template = "_entry:\n" +
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
     * Creates the necessary directories for output files if they don't exist.
     * @throws IOException if an issue with IO occurs.
     */
    private void createOutputDirs() throws IOException {
        Files.createDirectories(outputDirectoryPath.resolve(sourceName + File.separator + "bin"));
    }

    /**
     * Splits a line if it is a label followed by something other than an assembler directive.
     * @param line A {@link String} line comments removed and not containing semicolons allowing multiple instructions in the same line.
     * @return A {@link Stream} of the split {@link String} to be flattened.
     */
    private Stream<String> splitIfLabelNotWithAssemblerDirective(String line) {
        String[] split = line.split(":(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", 1);
        if (split.length < 2)
            return Stream.of(line);

        split[0] += ':';
        boolean containsDirective = split[1].trim().startsWith(".");

        if (containsDirective) {
            return Stream.of(split[0] + split[1]);
        }

        return Stream.of(split);
    }
}
