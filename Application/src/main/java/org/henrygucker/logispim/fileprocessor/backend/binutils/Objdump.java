package org.henrygucker.logispim.fileprocessor.backend.binutils;

import org.henrygucker.logispim.fileprocessor.backend.OSDependentData;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Class for disassembling MIPS ELF files
 */
public class Objdump {
    private static final String OBJDUMP_INLINE_SRC_PREFIX = "|";

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
