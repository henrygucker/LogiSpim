package org.henrygucker.logispim.fileprocessor.backend;

import org.henrygucker.logispim.fileprocessor.backend.exceptions.CompilationException;

import java.io.*;
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
 * Class storing static methods for cross-compiling a single-file MIPS program as well as performing
 * object dump operations on compiled ELF files.
 */
public class MIPSCrossCompiler {

    private static final String OBJDUMP_INLINE_SRC_PREFIX = "|";

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
    public static final List<String> OBJDUMP_DISASSEMBLY_FLAGS = List.of(
            "--source-comment=" + OBJDUMP_INLINE_SRC_PREFIX + "",
            "-M",
            "no-aliases",
            "--disassemble-zeroes",
            "--inlines",
            "-ld"
    );

    public static final List<String> OBJDUMP_DATA_FLAGS = List.of(
            "--disassemble-zeroes",
            "-s"
    );

    private enum ObjdumpState {
       AWAITING_SECTION,

        READING_LABEL,
        START_DIFF,
        READING_SOURCE,
        READING_ASSEMBLY,
    }

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

    /**
     * Fetches the data of one section of an ELF file in a readable form.
     * @param elfPath {@link Path} to ELF file
     * @param additionalFlags {@link List}{@code <}{@link String}{@code >} where each item is a commandline argument for objdump.
     *                                                                    Enter {@code null} when inserting no additional arguments.
     *                                                                    Note that use of additional flags may result
     *                                                                    in errors or unintended behavior.
     * @param useDefaultFlags uses {@link MIPSCrossCompiler#OBJDUMP_DATA_FLAGS} placed before {@code additionalFlags}
     *                        when {@code true}. Note that failing to use default flags may cause errors.
     * @param sectionName {@link String} specifying the section to decompile
     * @return section binary content output in {@link String} format
     * @throws IOException If an issue occurs with IO while disassembling
     * @throws InterruptedException if thread is interrupted while objdump is running
     */
    public static String objdumpData(Path elfPath, List<String> additionalFlags, boolean useDefaultFlags, String sectionName) throws IOException, InterruptedException {
        OSDependentData osDependentData = OSDependentData.getForCurrentOS();

        Process objdump = new ProcessBuilder(
                 Stream.of(
                         // Stream containing objdump executable path
                         Stream.of(
                                 osDependentData.getObjdumpPath().toString()
                         ),

                         // Stream of default flags if specified to use default flags
                         ((useDefaultFlags) ? OBJDUMP_DATA_FLAGS : List.of()).stream(),

                         // Stream of additional flags if additionalFlags List is not null
                         (additionalFlags != null) ? additionalFlags.stream() : List.of().stream(),

                         // Stream of arguments specifying to target the specified section & the ELF file target
                         Stream.of(
                                 "-j",
                                 sectionName,
                                 elfPath.toString()
                         )
                )
                        .flatMap(Function.identity())            // Flattens nested streams into one stream

                        .map(self -> self.toString())    // Essentially just maps Stream<Object> to Stream<String>
                                                                // but all Objects are already Strings
                        .toList()
        ).start();


        // Only stores lines in data and text sections
        StringBuilder dataBuilder = new StringBuilder();
        long currentLineNumber = 0;
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(objdump.getInputStream()))) {

            String line = bufferedReader.readLine();
            ObjdumpState state = ObjdumpState.AWAITING_SECTION;
            while (line != null) {
                switch (state) {
                    case AWAITING_SECTION -> {
                        if (!line.equals("Contents of section " + sectionName + ":")) {
                            line = bufferedReader.readLine();
                            continue;
                        }

                        line = bufferedReader.readLine();
                        state = ObjdumpState.READING_ASSEMBLY;
                    }
                    case READING_ASSEMBLY -> {
                        dataBuilder.append(line);
                        line = bufferedReader.readLine();
                    }
                }
            }
        }

        // Set timeout on objdump execution
        objdump.waitFor(5, TimeUnit.SECONDS);

        // Check if objdump exceeded timeout
        if (objdump.isAlive()) {
            objdump.destroy();

            return "Error when fetching instructions.\n" +
                    "Runtime limit exceeded.";
        }

        return dataBuilder.toString();
    }

    /**
     * Disassembles one section of an ELF file into a form with separate .
     * @param elfPath {@link Path} to ELF file
     * @param additionalFlags {@link List}{@code <}{@link String}{@code >} where each item is a commandline argument for objdump.
     *                                                                    Enter {@code null} when inserting no additional arguments.
     * @param useDefaultFlags uses {@link MIPSCrossCompiler#OBJDUMP_DATA_FLAGS} placed before {@code additionalFlags}
     *                        when {@code true}
     * @param sectionName {@link String} specifying the section to decompile
     * @return section binary content output in {@link String} format
     * @throws IOException If an issue occurs with IO while disassembling
     * @throws InterruptedException if thread is interrupted while objdump is running
     */
    public static List<DisassemblyLine> objdumpInstructions(Path elfPath, List<String> additionalFlags, boolean useDefaultFlags, String sectionName) throws IOException, InterruptedException {
        OSDependentData osDependentData = OSDependentData.getForCurrentOS();

        Process objdump = new ProcessBuilder(
                 Stream.of(
                         // Stream containing objdump executable path
                         Stream.of(
                                 osDependentData.getObjdumpPath().toString()
                         ),

                         // Stream of default flags if specified to use default flags
                         ((useDefaultFlags) ? OBJDUMP_DISASSEMBLY_FLAGS : List.of()).stream(),

                         // Stream of additional flags if additionalFlags List is not null
                         (additionalFlags != null) ? additionalFlags.stream() : List.of().stream(),

                         // Stream of arguments specifying to target the specified section & the ELF file target
                         Stream.of(
                                 "-j",
                                 sectionName,
                                 elfPath.toString()
                         )
                )
                        .flatMap(Function.identity())            // Flattens nested streams into one stream

                        .map(self -> self.toString())    // Essentially just maps Stream<Object> to Stream<String>
                                                                // but all Objects are already Strings
                        .toList()
        ).start();


        // Only stores lines in data and text sections
        ArrayList<DisassemblyLine> disassembly = new ArrayList<>();

        // Builders for each DisassemblyLine
        ArrayList<String> assemblyBuilder = new ArrayList();
        String currSourceLine = "";
        long currentLineNumber = 0;

        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(objdump.getInputStream()))) {
            String line = bufferedReader.readLine();
            ObjdumpState state = ObjdumpState.AWAITING_SECTION;
            while (line != null) {
                switch (state) {
                    case AWAITING_SECTION -> {
                        if (!line.equals("Disassembly of section " + sectionName + ":")) {
                            line = bufferedReader.readLine();
                            continue;
                        }

                        line = bufferedReader.readLine();

                        // Line after diassembly of section starting should be an empty line (log if not)
                        if (!line.isBlank()) {
                            System.err.println("Line after section beginning was not blank within disassembly.");
                            System.err.println("Line: \"" + line + "\"");
                        }

                        state = ObjdumpState.READING_LABEL;
                    }
                    case READING_LABEL -> {
                        if (line.isBlank()) {
                            line = bufferedReader.readLine();
                            continue;
                        }

                        // Create diff with only assembly (label)
                        disassembly.add(new DisassemblyLine(null, List.of(line), null));

                        // Finds line which starts next inline (usually only has to run once)
                        while ((line = bufferedReader.readLine()) != null && -1 == getInlineLineNumber(line)) {}
                        state = ObjdumpState.START_DIFF;
                    }
                    case START_DIFF -> {
                        // Assumes line is of the format:
                        // /path/to/source/file.s:#
                        //
                        // where # is line number

                        // Starts the process of reading a line of disassembled source code
                        currentLineNumber = getInlineLineNumber(line); // Finds the line number of the current source line
                        assemblyBuilder.clear();        // Resets the assembly builder without reallocating
                        // currSourceLine will be overwritten on its own, so we don't need to reset that

                        line = bufferedReader.readLine();
                        state = ObjdumpState.READING_SOURCE;
                    }
                    case READING_SOURCE -> {
                        // All source lines start with | character
                        if (!line.startsWith(OBJDUMP_INLINE_SRC_PREFIX)) {

                            // By this point, currSourceLine stores the last line of printed source code
                            state = ObjdumpState.READING_ASSEMBLY;
                            continue;
                        }

                        // Update current source line
                        // Lines usually come out in the following format:
                        // |        li $v0, 4

                        // Trims prefix and whitespace surrounding source code
                        currSourceLine = line.substring(OBJDUMP_INLINE_SRC_PREFIX.length()).trim();

                        line = bufferedReader.readLine();
                    }
                    case READING_ASSEMBLY -> {

                        // Reached label (end of current assembly inline)
                        if (line.isBlank()) {

                            // Create disassembly record (using copies of reference types)
                            disassembly.add(
                                    new DisassemblyLine(
                                            currentLineNumber,
                                            (ArrayList<String>) assemblyBuilder.clone(),
                                            "" + currSourceLine
                                    )
                            );
                            state = ObjdumpState.READING_LABEL;
                            line = bufferedReader.readLine();
                            continue;
                        }

                        // Reached start of next inline
                        if (-1 != getInlineLineNumber(line)) {

                            // Create disassembly record (using copies of reference types)
                            disassembly.add(
                                    new DisassemblyLine(
                                            currentLineNumber,
                                            (ArrayList<String>) assemblyBuilder.clone(),
                                            "" + currSourceLine
                                    )
                            );
                            state = ObjdumpState.START_DIFF;
                            continue;
                        }

                        assemblyBuilder.add(line);
                        line = bufferedReader.readLine();
                    }
                }
            }
        }

        // Set timeout on objdump execution
        objdump.waitFor(5, TimeUnit.SECONDS);

        // Check if objdump exceeded timeout
        if (objdump.isAlive()) {
            objdump.destroy();
            System.err.println("Objdump execution timed out.");
        }

        return disassembly;
    }

    /**
     * When source code is inlined by objdump, each inline begins with the following format:<br>
     * <br>
     * {@code /Path/to/file.s:#}<br>
     * <br>
     * This function parses a {@link String} to see if it is of this format, and if so, return the line number.
     * @param line {@link String to be tested}
     * @return the line number specified by the string if found. Otherwise, returns {@code -1}.
     */
    private static long getInlineLineNumber(String line) {
        // Splits at all colons not inside strings
        String[] split = line.split(":(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", 2);

        // Must have split
        if (split.length < 2)
            return -1;

        Path inlineFileLocation = null;
        try {
            inlineFileLocation = Path.of(split[0]);
        } catch (InvalidPathException e) {
            // Must be a valid path (logs if failure)
            e.printStackTrace();

            return -1;
        }

        if (inlineFileLocation == null || !inlineFileLocation.toFile().isFile())
            return -1;

        long lineNum = -1;
        try {
            lineNum = Long.parseLong(split[1]);
        } catch (NumberFormatException e) {
            // Must be valid format (logs if failure)
            e.printStackTrace();

            return -1;
        }

        return lineNum;
    }
}
