package org.henrygucker.logispim.fileprocessor.backend;

import org.henrygucker.logispim.fileprocessor.App;
import org.henrygucker.logispim.fileprocessor.backend.elf.ElfFile;
import org.henrygucker.logispim.fileprocessor.backend.elf.ElfFormatException;
import org.henrygucker.logispim.fileprocessor.backend.elf.SectionHeader;
import org.henrygucker.logispim.fileprocessor.backend.elf.Symbol;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.HashMap;

/**
 * Class whose instances provide utilities for parsing ELF files to generate HEX files for import to Logisim-evolution
 */
public class HexFileCompiler {
    private final App app;

    public HexFileCompiler(App app) {
        this.app = app;
    }

    /**
     * Generates .hex files to be imported to Logisim for kernel programs
     * @param elfFile The {@link Path} to the ELF file of the compiled program
     * @param kernelTextBinaryFile The {@link Path} to the HEX file which will store the kernel text section of the compiled program
     */
    public void generateKernelProgramHexFiles(ElfFile elfFile, Path kernelTextBinaryFile) {
        ByteBuffer currSectionContent = null;

        // Fetching text section content
        try {
            currSectionContent = elfFile.getSectionContent(".text");
        } catch (IOException e) {
            app.errorMessage("Compilation Error", "An error occurred while reading compiled ELF file. Try again.");
            return;
        } catch (ElfFormatException | IllegalArgumentException e) {
            app.errorMessage("Compilation Error", "Unable to locate .text section in compiled input file.");
            return;
        }

        // Needs to overwrite words at following symbols with these big-endian values as little-endian values:
        // - exit: 0x50000000
        // - print_char: 0x54000000
        HashMap<String, Integer> symbolOffsets = new HashMap<>();
        HashMap<String, Integer> symbolReplacements = new HashMap<>();
        symbolReplacements.put("exit", 0x50000000);
        symbolReplacements.put("print_char", 0x54000000);

        // Gets start of text section (should always be 0x80000000, but checks to make sure)
        int textSectionStart = 0x80000000;
        for (SectionHeader sh : elfFile.sectionHeaders)
            if (elfFile.getSectionName(sh).equals(".text")) {
                textSectionStart = sh.addr();
                break;
            }

        // Gets offsets of symbols with names as keys in symbolReplacements and stores in symbolOffsets
        for (Symbol sym : elfFile.symbols) {
            symbolOffsets.put(elfFile.getSymbolName(sym), sym.value() - textSectionStart);
        }

        // Replaces words at symbol locations in ByteBuffer
        for (String symbolName : symbolOffsets.keySet()) {
            if (!symbolReplacements.containsKey(symbolName))
                continue;

            currSectionContent.putInt(symbolOffsets.get(symbolName), symbolReplacements.get(symbolName));
        }

        try {
            HexFileCompiler.writeBufferToLogisimHexFile(currSectionContent, kernelTextBinaryFile);
        } catch (IOException e) {
            app.errorMessage("Compilation Error", "An error occurred while writing \"text-kernel\" binary file.");
        }

        return;
    }

    /**
     * Generates .hex files to be imported to Logisim for user-space programs
     * @param elfFile The {@link Path} to the ELF file of the compiled program
     * @param textBinaryFile The {@link Path} to the HEX file which will store the text section of the compiled program
     * @param dataBinaryFile The {@link Path} to the HEX file which will store the data section of the compiled program
     */
    public void generateUserProgramHexFiles(ElfFile elfFile, Path textBinaryFile, Path dataBinaryFile) {
        ByteBuffer currSectionContent = null;

        // Fetching text section content
        try {
            currSectionContent = elfFile.getSectionContent(".text");
        } catch (IOException e) {
            app.errorMessage("Compilation Error", "An error occurred while reading compiled ELF file. Try again.");
            return;
        } catch (ElfFormatException | IllegalArgumentException e) {
            app.errorMessage("Compilation Error", "Unable to locate .text section in compiled input file.");
            return;
        }

        // File writing for non-kernel text
        try {
            HexFileCompiler.writeBufferToLogisimHexFile(currSectionContent, textBinaryFile);
        } catch (IOException e) {
            app.errorMessage("Compilation Error", "An error occurred while writing \"text\" binary file.");
        }

        // Fetching data section content
        try {
            currSectionContent = elfFile.getSectionContent(".data");
        } catch (IOException e) {
            app.errorMessage("Compilation Error", "An error occurred while reading compiled ELF file. Try again.");
            return;
        } catch (ElfFormatException | IllegalArgumentException e) {
            app.errorMessage("Compilation Error", "Unable to locate .data section in compiled input file.");
            return;
        }

        // Writing data content to file
        try {
            HexFileCompiler.writeBufferToLogisimHexFile(currSectionContent, dataBinaryFile);
        } catch (IOException e) {
            app.errorMessage("Compilation Error", "An error occurred while writing \"data\" binary file.");
        }

    }

    /**
     * Writes the remaining content of a ByteBuffer to a file at the specified path following hex byte format for Logisim-Evolution.
     * @param byteBuffer Source of data to write.
     * @param file Destination file location.
     * @throws IOException If an issue with IO occurs.
     */
    public static void writeBufferToLogisimHexFile(ByteBuffer byteBuffer, Path file) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file.toFile()))) {
            writer.write("v3.0 hex bytes\n");
            while (byteBuffer.hasRemaining()) {
                writer.write(String.format("%02x ", Byte.toUnsignedInt(byteBuffer.get())));
            }
        }
    }
}
