package org.henrygucker.logispim.logispim.fileprocessor.backend.elf;

public class ElfFormatException extends Exception {
    public ElfFormatException(String filename, String message) {
        this((message == "")
                ? "\"" + filename + "\" is not a valid ELF file."
                : "An error occurred while reading from \"filename\".\n"
        );
    }

    public ElfFormatException(String message) {
        super(message);
    }
}
