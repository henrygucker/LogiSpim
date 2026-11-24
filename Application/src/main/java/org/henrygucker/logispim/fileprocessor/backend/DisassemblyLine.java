package org.henrygucker.logispim.fileprocessor.backend;

import java.util.List;

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
                long desiredTabs = 3;
                long numTabs = sourceLine.chars().filter(c -> c == '\t').count();

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
}
