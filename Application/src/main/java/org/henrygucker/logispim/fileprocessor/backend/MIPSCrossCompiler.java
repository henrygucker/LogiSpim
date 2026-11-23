package org.henrygucker.logispim.fileprocessor.backend;

import org.henrygucker.logispim.fileprocessor.backend.exceptions.CompilationException;

import java.io.*;
import java.lang.reflect.Array;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Class storing static method for cross-compiling a single-file MIPS program
 */
public class MIPSCrossCompiler {

    private static final List<String> asFlags = List.of(
                "-aghilms",
                "--gdwarf-2",
                "-mips1",
                "-O0",
                "-msoft-float"
    );
    private static final List<String> ldFlags = List.of(
            "-g"
    );
    private static final List<String> objdumpFlags = List.of(
            "--source-comment",
            "-M",
            "no-aliases",
            "-z"
//            "--inlines",
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
                        asFlags.stream(),
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
                        ldFlags.stream(),
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
                boolean innContainsComment = Pattern.compile("#(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)").matcher(inn).find();

                for (String split : Arrays.stream(inn.split("#(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)")) // Splits each line at any # character not enclosed in quotes
                        .limit(1) // Limits stream to the substring before the first # found
                        .flatMap(str -> Arrays.stream(str.split(";(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)")))
                        .flatMap(str -> splitIfLabelWithoutAssemblerDirective(str))
                        .map(String::trim)
                        .filter(str -> str.length() != 0 || !innContainsComment)
                        .collect(Collectors.toCollection(ArrayList::new))
                ) {

                    // Insert before current instruction
                    if (!isKernelText && split.equals("syscall"))
                        bufferedWriter.write("\tmove $k0, $ra # Inserted by preprocessor for syscall\n");

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

    public static String objdumpAll(Path elfPath) throws IOException {
        return objdump(elfPath, List.of("-D"), true, null);
    }

    public static String objdumpSection(Path elfPath, String sectionName) throws IOException {
        return objdump(elfPath, List.of("-j", sectionName), true, sectionName);
    }

    public static String objdumpSectionWithExtraFlags(Path elfPath, List<String> additionalFlags, String sectionName) throws IOException {
        List<String> combinedFlagsWithSectionSpecifier = Stream.of(
                additionalFlags.stream(),
                Stream.of("-j", sectionName)
        ).flatMap(Function.identity())
                .toList();

        return objdump(elfPath, combinedFlagsWithSectionSpecifier, true, sectionName);
    }

    public static String objdumpWithCustomFlags(Path elfPath, List<String> flags) throws IOException {
        return objdump(elfPath, flags, false, null);
    }

    private static String objdump(Path elfPath, List<String> additionalFlags, boolean useDefaultFlags, String filterSection) throws IOException {
        OSDependentData osDependentData = OSDependentData.getForCurrentOS();

        System.out.println(
                 Stream.of(
                         // Creates stream of streams which will be flattened
                         Stream.of(
                                 osDependentData.getObjdumpPath().toString()
                         ),
                         ((useDefaultFlags) ? objdumpFlags : List.of()).stream(),
                         additionalFlags.stream(),
                         Stream.of(
                                 elfPath.toString()
                         )
                )
                        .flatMap(Function.identity())
                        .map(self -> self.toString())
                        .toList()
        );

        Process objdump = new ProcessBuilder(
                 Stream.of(
                         // Creates stream of streams which will be flattened
                         Stream.of(
                                 osDependentData.getObjdumpPath().toString()
                         ),
                         ((useDefaultFlags) ? objdumpFlags : List.of()).stream(),
                         additionalFlags.stream(),
                         Stream.of(
                                 elfPath.toString()
                         )
                )
                        .flatMap(Function.identity())
                        .map(self -> self.toString())
                        .toList()
        ).start();


        // Only stores lines in data and text sections
        boolean shouldStoreLines = true;
        StringBuilder objdumpOutputBuilder = new StringBuilder();
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(objdump.getInputStream()))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {

                if (
                        filterSection == null ||
                                line.equals("Disassembly of section " + filterSection + ":") ||
                                line.equals("Contents of section " + filterSection + ":")

                ) {
                    // Start storing lines at start of desired section
                    shouldStoreLines = true;
                }
//                } else if (
//                        filterSection != null &&
//                                (line.startsWith("Disassembly of section ") || line.startsWith("Contents of section "))
//                ) {
//                    // Stop storing lines at start of other sections
//                    shouldStoreLines = false;
//                }

                // Store line if past start of section (if filtering)
                if (true) {

                    // Inserts line
                    objdumpOutputBuilder.append(' '); // Ensures conformity with Logisim-evolution Assembly Viewer regex
                    objdumpOutputBuilder.append(line);

                    // Inject comment into objdump to explain that syscall instruction has been wrapped
                    if (
                            filterSection != null &&
                            filterSection.equals(".text") &&
                            line.contains("03e0d025")
                    )  {
                        // move instruction inserted before syscall
                        objdumpOutputBuilder.append("\t# (syscall wrapper)");
                    }

                    if (
                            filterSection != null &&
                            filterSection.equals(".text") &&
                                    line.contains("<main>:")
                    ) {
                        objdumpOutputBuilder.append("\t\t\t# YOUR PROGRAM STARTS HERE");
                    }


                    objdumpOutputBuilder.append('\n');
                }
            }
        } catch (Exception e) {
            e.printStackTrace();

            throw e;
        }

        // Set timeout on objdump execution
        try {
            objdump.waitFor(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            return "Error when fetching instructions.\n" +
                    "Worker thread interrupted.";
        }

        // Check if objdump exceeded timeout
        if (objdump.isAlive()) {
            objdump.destroy();

            return "Error when fetching instructions.\n" +
                    "Runtime limit exceeded.";
        }

        return objdumpOutputBuilder.toString();
    }
}
