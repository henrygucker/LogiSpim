package org.henrygucker.logispim.fileprocessor.backend.binutils;

import org.henrygucker.logispim.fileprocessor.backend.OSDependentData;
import org.henrygucker.logispim.fileprocessor.backend.exceptions.CompilationException;

import java.io.*;
import java.nio.file.*;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Class storing static methods for cross-compiling a single-file MIPS program into ELF files
 */
public class MIPSCrossCompiler {
    private static final List<String> ASSEMBLER_FLAGS = List.of(
                "-agilms",
                "--gdwarf-2",
                "-mips1",
                "-O0",
                "-msoft-float",
                "--alternate"
    );
    private static final List<String> LINKER_FLAGS = List.of(
            "-g"
    );

    /**
     * Compiles the file located at the entered source path into an ELF file.
     * @param sourcePath A {@link Path} to the source MIPS assembly file containing a main function that will be called.
     * @param linkerScriptPath A {@link Path} to the linker script that will be used by the linker.
     * @param outputDirectoryPath The home directory for all outputs.
     * @param isKernelText true if {@code  sourcePath} is to be compiled to kernel instructions.
     * @return the {@link Path} to the generated ELF file.
     * @throws IOException if an issue with IO occurs.
     * @throws InterruptedException if the thread working on compiling is interrupted
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

        // Creates /sourceName/bin directory and all parent directories
        Files.createDirectories(binDir);

        // Preprocess source code
        preprocessor(sourcePath, preProcessedAsmFilePath, isKernelText);

        // Assemble source file
        assemble(preProcessedAsmFilePath, objectFilePath);

        // Linking
        link(new Path[]{objectFilePath}, linkerScriptPath, elfFilePath);

        return elfFilePath;
    }

    /**
     * Assembles the specified source code file
     * @param sourceAssemblyFile {@link Path} to MIPS source code to compile
     * @param outputObjectFile {@link Path} to location of resulting object file
     * @throws IOException if an issue with IO occurs.
     * @throws InterruptedException if the thread working on compiling is interrupted
     * @throws CompilationException if an error occurs within the assembler.
     */
    public static void assemble(Path sourceAssemblyFile, Path outputObjectFile) throws CompilationException, IOException, InterruptedException {
        OSDependentData osDependentData = OSDependentData.getForCurrentOS();

        Process assembler = new ProcessBuilder(
                Stream.of(
                        // Creates stream of streams which get flattened
                        Stream.of(
                                osDependentData.getAssemblerPath().toString()
                        ),
                        ASSEMBLER_FLAGS.stream(),
                        Stream.of(
                                "-o",
                                outputObjectFile.toString(),
                                sourceAssemblyFile.toString()
                        )
                )
                .flatMap(Function.identity())
                .toList()
        ).start();

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

        assembler.waitFor(5, TimeUnit.SECONDS);

        if (assembler.isAlive()) {
            assembler.destroy();

            throw new CompilationException("Assembler Error", "Runtime Limit Exceeded.");
        }
    }

    /**
     * Links the specified object files
     * @param objectFiles {@link Path}{@code []} storing locations of object files to link
     * @param outputObjectFile {@link Path} to location of resulting ELF file
     * @throws IOException if an issue with IO occurs.
     * @throws InterruptedException if the thread working on compiling is interrupted
     * @throws CompilationException if an error occurs within the linker.
     */
    public static void link(Path[] objectFiles, Path linkerScript, Path outputElfFile) throws CompilationException, IOException, InterruptedException {
        OSDependentData osDependentData = OSDependentData.getForCurrentOS();

        Process linker = new ProcessBuilder(
                Stream.of(
                        // Creates stream of streams which will be flattened
                        Stream.of(
                                osDependentData.getLinkerPath().toString()
                        ),
                        LINKER_FLAGS.stream(),
                        Stream.of(
                                "-T",
                                linkerScript.toString(),
                                "-o",
                                outputElfFile.toString()
                        ),
                        Stream.of(objectFiles).map(path -> path.toString())
                )
                .flatMap(Function.identity())
                .collect(Collectors.toList())
        ).start();

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

        linker.waitFor(5, TimeUnit.SECONDS);

        if (linker.isAlive()) {
            linker.destroy();

            throw new CompilationException("Assembler Error", "Runtime Limit Exceeded.");
        }
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
        final String template = "_start:\n" +
                "\tjal main\n" +
                "\n" +
                "\t# Exit program\n" +
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
                final boolean innContainsComment = Pattern.compile("#(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)").matcher(inn).find();
                boolean hasToWriteComment = innContainsComment;

                for (String split : Arrays.stream(inn.split("#(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", 2)) // Splits each line first # character not enclosed in quotes
                        .limit(1) // Ignores commented split
                        .flatMap(str -> Arrays.stream(str.split(";(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)"))) // Splits at any semicolons not enclosed in quotes
                        .flatMap(str -> splitIfLabelWithoutAssemblerDirective(str)) // Splits after colon if not followed by assembler directive (which starts with a .)
                        .map(String::trim)
                        .filter(str -> str.length() != 0 || !innContainsComment) // Skips entirely empty lines
                        .toList()
                ) {

                    // At this point, split must be the source code of the line read by inn.
                    // If split is an empty string, inn must have contained a comment.

                    // Insert before current instruction
                    if (!isKernelText && split.equals("syscall"))
                        bufferedWriter.write("\tmove $k0, $ra # [inserted for syscall]\n");

                    // Inserts a tab before non-labels code which does not contain a colon (excluding colons inside strings)
                    if (!Pattern.compile(":(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)").matcher(split).find())
                        bufferedWriter.write('\t');

                    // Writes current instruction
                    bufferedWriter.write(split.trim());

                    // If inn had a comment which hasn't been written yet, write it.
                    if (hasToWriteComment) {
                        hasToWriteComment = false;
                        
                        String[] splits = inn.split("#(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", 2);
                        bufferedWriter.write("\t# ");
                        bufferedWriter.write(splits[1]);
                    }

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
    private static Stream<String> splitIfLabelWithoutAssemblerDirective(String line) {
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
