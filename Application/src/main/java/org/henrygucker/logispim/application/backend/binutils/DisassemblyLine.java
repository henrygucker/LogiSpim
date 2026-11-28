package org.henrygucker.logispim.application.backend.binutils;

import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyledDocument;
import java.util.List;

/**
 * Record for storing the resulting disassembly from an associated line of source code.
 * @param sourceLineNumber Stores the line number from the source assembly code associated with this object.
 *                         Stores {@code null} if the object stores a label.
 *                         Stores {@code -1} if the object stores a line inserted by the preprocessor.
 * @param assemblyLines A {@link List} of lines of assembly resulting from the compilation of the one line of source code.
 * @param sourceLine The {@link String} line of source code associated with this object.
 *                   Stores {@code null} if the object stores a label.
 */
public record DisassemblyLine(Long sourceLineNumber, List<String> assemblyLines, String sourceLine) {

    public void appendToStyledDocument(StyledDocument doc, Style baseTextStyle, Style lineNumberStyle, Style inlinedTextStyle) {

        // Inserts newline if object has no source line number (is label)
        if (sourceLineNumber == null)
            try {
                doc.insertString(doc.getLength(), "\n", baseTextStyle);
            } catch (BadLocationException e) { e.printStackTrace(); /* Shouldn't happen ever */}

        for (int i = 0; i < assemblyLines.size(); i++) {
            // Print current assembly line
            try {
                doc.insertString(doc.getLength(), assemblyLines.get(i), baseTextStyle);
            } catch (BadLocationException e) { e.printStackTrace(); /* Shouldn't happen ever */}

            // Write source line if it exists and is writing the first line of assembly
            if (i == 0 && sourceLine != null) {

                // Insert appropriate number of tabs to align source code with other source lines
                long desiredTabs = 5;
                long numTabs = lineIndentWidth(assemblyLines.get(i), 11);
                for (long j = numTabs; j < desiredTabs; j++) {
                    try {
                        doc.insertString(doc.getLength(), "\t", baseTextStyle);
                    } catch (BadLocationException e) { e.printStackTrace(); /* Shouldn't happen ever */}
                }

                // Write source
                String[] split = sourceLine.trim().split("#", 2);
                try {
                    doc.insertString(doc.getLength(), ";", lineNumberStyle);

                    // Writes line num (if exists)
                    if (sourceLineNumber != null && sourceLineNumber >= 0)
                        doc.insertString(doc.getLength(), String.format("%4d: ", sourceLineNumber), lineNumberStyle);
                    else
                        doc.insertString(doc.getLength(), " xxx: ", lineNumberStyle);

                    // Writes line (pre-comment)
                    if (!split[0].trim().equals("move $k0, $ra; syscall; nop"))
                        doc.insertString(doc.getLength(), split[0], inlinedTextStyle);
                    else
                        doc.insertString(doc.getLength(), "syscall\t", inlinedTextStyle);

                    // Writes line (post-comment)
                    if (split.length > 1) {
                        doc.insertString(doc.getLength(), "#", lineNumberStyle);
                        doc.insertString(doc.getLength(), split[1], lineNumberStyle);
                    }

                } catch (BadLocationException e) { e.printStackTrace(); /* Shouldn't happen ever */}
            }

            // Write newline char
            try {
                doc.insertString(doc.getLength(), "\n", baseTextStyle);
            } catch (BadLocationException e) { e.printStackTrace(); /* Shouldn't happen ever */}
        }
    }

    /**
     * Converts this {@link DisassemblyLine} to a {@link String} for writing into {@code .lss} file which Logisim-evolution can use
     * @return {@String} with newline character at the end containing this record formatted for use in
     * {@code .lss} disassembly file.
     */
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < assemblyLines.size(); i++) {
            builder.append(assemblyLines.get(i));

            // Write source line if it exists and is writing the first line of assembly
            if (i == 0 && sourceLine != null) {
                long desiredTabs = 7;
                long numTabs = lineIndentWidth(assemblyLines.get(i), 8);

                for (long j = numTabs; j < desiredTabs; j++) {
                    builder.append('\t');
                }

                // Writes source line (excludes comments)
                builder.append("; ");
                String[] splits = sourceLine.trim().split("#", 2);

                // Writes line (pre-comment)
                if (!splits[0].trim().equals("move $k0, $ra; syscall; nop"))
                    builder.append(splits[0].trim());
                else
                    builder.append("syscall # [Preprocessor Expansion]");
            }

            builder.append('\n');
        }

        return builder.toString();
    }

    /**
     * Computes the amount of indents (assuming 8 characters per indent) a string fully occupies.
     * @param line {@link String} to check; must be one line
     * @param numCharsPerIndent specifies the number of monospaced characters which occupy the same width as one indent.
     *                          This varies across different types of text fields.
     * @return the number of indents a string occupies
     */
    private static int lineIndentWidth(String line, int numCharsPerIndent) {
        int currChars = 0;
        int numIndents = 0;

        for (char c : line.toCharArray()) {
            if (c == '\t' || currChars == numCharsPerIndent - 1) {
                numIndents++;
                currChars = 0;
                continue;
            }

            currChars++;
        }

        return numIndents + (currChars / numCharsPerIndent);
    }
}
