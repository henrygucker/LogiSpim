package org.henrygucker.logispim.fileprocessor.backend;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * Record for storing the resulting disassembly from an associated line of source code.
 * @param sourceLineNumber Stores the line number from the processed assembly code associated with this object.
 *                         Stores {@code null} if the object stores a label.
 * @param assemblyLines A {@link List} of lines of assembly resulting from the compilation of the one line of source code.
 * @param sourceLine The {@link String} line of source code associated with this object.
 *                   Stores {@code null} if the object stores a label.
 */
public record DisassemblyLine(Long sourceLineNumber, List<String> assemblyLines, String sourceLine) {

    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < assemblyLines.size(); i++) {
            builder.append(assemblyLines.get(i));

            // Write source line if it exists and is writing the first line of assembly
            if (i == 0 && sourceLine != null) {
                long desiredTabs = 8;
                long numTabs = lineIndentWidth(assemblyLines.get(i));

                for (long j = numTabs; j < desiredTabs; j++) {
                    builder.append('\t');
                }
                builder.append("| ");
                builder.append(sourceLine.trim());
            }

            builder.append('\n');
        }

        return builder.toString();
    }

    /**
     * Computes the amount of indents (assuming 8 characters per indent) a string fully occupies.
     * @param line {@link String} to check; must be one line
     * @return the number of indents a string occupies
     */
    private static int lineIndentWidth(String line) {
        final int NUM_CHARS_PER_INDENT = 8;
        int currChars = 0;
        int numIndents = 0;

        for (char c : line.toCharArray()) {
            if (c == '\t' || currChars == NUM_CHARS_PER_INDENT - 1) {
                numIndents++;
                currChars = 0;
                continue;
            }

            currChars++;
        }

        return numIndents + (currChars / NUM_CHARS_PER_INDENT);
    }
}
