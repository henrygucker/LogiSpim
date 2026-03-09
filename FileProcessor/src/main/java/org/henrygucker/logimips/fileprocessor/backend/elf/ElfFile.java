package org.henrygucker.logimips.fileprocessor.backend.elf;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ElfFile {
    private final String filename;
    private final Path filepath;

    public final ElfHeader header;
    public final SectionHeader[] sectionHeaders;
    public final ProgramHeader[] programHeaders;
    public final Symbol[] symbols;

    public final StringTable sectionHeaderStringTable;
    public final StringTable stringTable;


    private final int ELF_HEADER_SIZE = 52;

    public ElfFile(Path filepath) throws IOException, ElfFormatException {
        this.filename = filepath.toString();
        this.filepath = filepath;

        header = getElfHeader();
        sectionHeaders = getSectionHeaders();
        programHeaders = getProgramHeaders();
        symbols = getSymbols();

        sectionHeaderStringTable = getSectionHeaderStringTable();
        stringTable = getStringTable();
    }

    public ElfFile(String filename) throws IOException, ElfFormatException {
        this(Paths.get(filename));
    }

    public String getSectionName(SectionHeader sectionHeader) {
        return sectionHeaderStringTable.getString(sectionHeader.name());
    }

    public String getSymbolName(Symbol symbol) {
        return stringTable.getString(symbol.name());
    }

    public ByteBuffer getSectionContent(String sectionName) throws IllegalArgumentException, IOException, ElfFormatException {
        // Getting associated section header
        SectionHeader sectionHeader = null;
        for (SectionHeader sh : sectionHeaders) {
            if (getSectionName(sh).equals(sectionName)) {
                sectionHeader = sh;
            }
        }

        if (sectionHeader == null) {
            throw new IllegalArgumentException("There does not exist a section named \"" + sectionName + "\".");
        }


        try {
            return getSectionContent(sectionHeader);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IllegalArgumentException("The entered index does not correspond to a section.");
        } catch (IOException e) {
            throw e;
        }
    }

    // based on index in section header table
    public ByteBuffer getSectionContent(int index) throws IllegalArgumentException, IOException, ElfFormatException {
        try {
            return getSectionContent(sectionHeaders[index]);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IllegalArgumentException("The entered index does not correspond to a section.");
        } catch (IOException e) {
            throw e;
        }
    }

    public ByteBuffer getSectionContent(SectionHeader sectionHeader) throws IllegalArgumentException, IOException, ElfFormatException {
        // Only applies to instances of section headers in the sectionHeaders array

        boolean seenSectionHeader = false;
        for (SectionHeader sh : sectionHeaders) {
            if (sh == sectionHeader) {
                seenSectionHeader = true;
                break;
            }
        }

        if (!seenSectionHeader) {
            throw new IllegalArgumentException("The entered SectionHeader instance is not in the sectionHeaders array.");
        }

        ByteBuffer out = null;
        try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(filename))) {
            if (inputStream.skip(sectionHeader.offset()) != sectionHeader.offset()) {
                inputStream.close();

                throw new ElfFormatException(filename, "Entered section header is inconsistent. Offset is larger than ELF file.");
            }

            out = ByteBuffer.wrap(inputStream.readNBytes(sectionHeader.size())).order(ByteOrder.LITTLE_ENDIAN);

        } catch (EOFException e) {
            throw new ElfFormatException(filename, "Section named " + sectionHeaderStringTable.getString(sectionHeader.name())
                                                + " has contents past the end of the file.");
        } catch (IOException e) {
            throw e;
        }

        out.position(0);

        return out;
    }

    private ElfHeader getElfHeader() throws IOException, ElfFormatException {
        ElfHeader header = null;
        try (DataInputStream inputStream = new DataInputStream(new BufferedInputStream(new FileInputStream(filename)))) {

            // Read into byte buffer to allow for values to be read according to the little-endian byte order
            ByteBuffer buff = ByteBuffer.wrap(inputStream.readNBytes(ELF_HEADER_SIZE)).order(ByteOrder.LITTLE_ENDIAN);

            // Gets identifier byte[] and moves buff position
            byte[] identifier = new byte[ElfHeader.getIdentifierSize()];
            buff = buff.get(identifier);

            header = new ElfHeader(
                    identifier,
                    buff.getShort(),
                    buff.getShort(),
                    buff.getInt(),
                    buff.getInt(),
                    buff.getInt(),
                    buff.getInt(),
                    buff.getInt(),
                    buff.getShort(),
                    buff.getShort(),
                    buff.getShort(),
                    buff.getShort(),
                    buff.getShort(),
                    buff.getShort()
            );
        } catch (EOFException e) {
            throw new ElfFormatException(filename, "File does not contain an Elf-32 Header.");
        } catch (IOException e) {
            throw e;
        }

        if (!isValidElfHeader(header)) {
            header = null;
            throw new ElfFormatException(filename, "This file lacks the proper header.");
        }

        return header;
    }

    private SectionHeader[] getSectionHeaders() throws IOException, ElfFormatException {
        if (header == null) {
            throw new ElfFormatException(filename, "An attempt to get the section headers from this file cannot be made until the ELF header has been read.");
        }

        SectionHeader[] sectionHeaders = null;

        try (DataInputStream inputStream = new DataInputStream(new BufferedInputStream(new FileInputStream(filename)))) {

            // Offsets reader to start of section header table
            if (inputStream.skip(header.shoff()) != header.shoff()) {
                inputStream.close();

                throw new ElfFormatException(filename, "Section header table does not exist.");
            }



            sectionHeaders = new SectionHeader[header.shnum()];
            for (int i = 0; i < header.shnum(); i++) {
                try {
                    // Read into byte buffer to allow for values to be read according to the little-endian byte order
                    ByteBuffer buff = ByteBuffer.wrap(inputStream.readNBytes(header.shentsize())).order(ByteOrder.LITTLE_ENDIAN);

                    sectionHeaders[i] = new SectionHeader(
                            buff.getInt(),
                            SectionHeaderType.fromInt(buff.getInt()),
                            buff.getInt(),
                            buff.getInt(),
                            buff.getInt(),
                            buff.getInt(),
                            buff.getInt(),
                            buff.getInt(),
                            buff.getInt(),
                            buff.getInt()
                    );
                } catch (EOFException e) {
                    throw new ElfFormatException(filename, "Section header table is said to contain "
                            + header.shnum() + " elements, but failed to read element with index " + i + ".");
                } catch (IOException e) {
                    throw e;
                }
            }
        } catch (IOException e) {
            throw e;
        }

        return sectionHeaders;
    }

    private ProgramHeader[] getProgramHeaders() throws IOException, ElfFormatException {
        if (header == null) {
            throw new ElfFormatException(filename, "An attempt to get the program headers from this file cannot be made until the ELF header has been read.");
        }
        if (sectionHeaders == null) {
            throw new ElfFormatException(filename, "An attempt to get the program headers from this file cannot be made until the section headers have been read.");
        }


        ProgramHeader[] programHeaders = null;

        try (DataInputStream inputStream = new DataInputStream(new BufferedInputStream(new FileInputStream(filename)))) {

            // Offsets reader to start of section header table
            if (inputStream.skip(header.phoff()) != header.phoff()) {
                inputStream.close();

                throw new ElfFormatException(filename, "Program header table does not exist in file.");
            }



            programHeaders = new ProgramHeader[header.phnum()];
            for (int i = 0; i < header.phnum(); i++) {
                try {
                    // Read into byte buffer to allow for values to be read according to the little-endian byte order
                    ByteBuffer buff = ByteBuffer.wrap(inputStream.readNBytes(header.phentsize())).order(ByteOrder.LITTLE_ENDIAN);

                    programHeaders[i] = new ProgramHeader(
                            buff.getInt(),
                            buff.getInt(),
                            buff.getInt(),
                            buff.getInt(),
                            buff.getInt(),
                            buff.getInt(),
                            buff.getInt(),
                            buff.getInt()
                    );
                } catch (EOFException e) {
                    throw new ElfFormatException(filename, "Program header table is said to contain "
                            + header.shnum() + " elements, but failed to read element with index " + i + ".");
                } catch (IOException e) {
                    throw e;
                }
            }
        } catch (IOException e) {
            throw e;
        }

        return programHeaders;
    }

    private Symbol[] getSymbols() throws IOException, ElfFormatException {
        if (header == null) {
            throw new ElfFormatException(filename, "An attempt to read the symbol table from this file cannot be made until the ELF header has been read.");
        }
        if (sectionHeaders == null) {
            throw new ElfFormatException(filename, "An attempt to read the symbol table from this file cannot be made until the section headers have been read.");
        }

        // Fetching symbol table header
        SectionHeader symbolTableHeader = null;
        for (SectionHeader sectionHeader : sectionHeaders) {
            if (sectionHeader.type() == SectionHeaderType.SYMTAB) {
                symbolTableHeader = sectionHeader;
                break;
            }
        }

        if (symbolTableHeader == null) {
            throw new ElfFormatException(filename, "No symbol table section header was found in the section header table.");
        }


        Symbol[] symbols = null;

        try (DataInputStream inputStream = new DataInputStream(new BufferedInputStream(new FileInputStream(filename)))) {

            // Offsets reader to start of section header table
            if (inputStream.skip(symbolTableHeader.offset()) != symbolTableHeader.offset()) {
                inputStream.close();

                throw new ElfFormatException(filename, "Symbol table does not exist in file.");
            }


            symbols = new Symbol[symbolTableHeader.size() / symbolTableHeader.entsize()];
            for (int i = 0; i < symbolTableHeader.size() / symbolTableHeader.entsize(); i++) {
                try {
                    // Read into byte buffer to allow for values to be read according to the little-endian byte order
                    ByteBuffer buff = ByteBuffer.wrap(inputStream.readNBytes(symbolTableHeader.entsize())).order(ByteOrder.LITTLE_ENDIAN);

                    symbols[i] = new Symbol(
                            buff.getInt(),
                            buff.getInt(),
                            buff.getInt(),
                            buff.get(),
                            buff.get(),
                            buff.getShort()
                    );
                } catch (EOFException e) {
                    throw new ElfFormatException(filename, "Symbol header table is said to contain "
                            + header.shnum() + " elements, but failed to read element with index " + i + ".");
                } catch (IOException e) {
                    throw e;
                }
            }
        } catch (IOException e) {
            throw e;
        }

        return symbols;
    }

    private StringTable getSectionHeaderStringTable() throws IOException, ElfFormatException {
        if (header == null) {
            throw new ElfFormatException(filename, "An attempt to read the section header string table from this file cannot be made until the ELF header has been read.");
        }
        if (sectionHeaders == null) {
            throw new ElfFormatException(filename, "An attempt to read the section header string table from this file cannot be made until the section headers have been read.");
        }

        StringTable sectionHeaderStringTable = null;
        try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(filename))) {

            // Offsets reader to start of string table
            if (inputStream.skip(sectionHeaders[header.shstrndx()].offset()) != sectionHeaders[header.shstrndx()].offset()) {
                inputStream.close();

                throw new ElfFormatException(filename, "Section header string table does not exist in file.");
            }


            // Entries to string table are single-byte US_ASCII characters
            sectionHeaderStringTable  = new StringTable(inputStream.readNBytes(sectionHeaders[header.shstrndx()].size()));
        } catch (EOFException e) {
            throw new ElfFormatException(filename, "Section header string table spans past end of file.");
        } catch (IOException e) {
            throw e;
        }

        return sectionHeaderStringTable ;
    }

    private StringTable getStringTable() throws IOException, ElfFormatException {
        if (header == null) {
            throw new ElfFormatException(filename, "An attempt to read the string table from this file cannot be made until the ELF header has been read.");
        }
        if (sectionHeaders == null) {
            throw new ElfFormatException(filename, "An attempt to read the string table from this file cannot be made until the section headers have been read.");
        }

        // Fetching string table header
        SectionHeader stringTableHeader = null;
        for (SectionHeader sectionHeader : sectionHeaders) {
            if (sectionHeader.type() == SectionHeaderType.STRTAB) {
                stringTableHeader = sectionHeader;
                break;
            }
        }

        if (stringTableHeader == null) {
            throw new ElfFormatException(filename, "No string table section header was found in the section header table.");
        }

        StringTable stringTable = null;
        try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(filename))) {

            // Offsets reader to start of string table
            if (inputStream.skip(stringTableHeader.offset()) != stringTableHeader.offset()) {
                inputStream.close();

                throw new ElfFormatException(filename, "String table does not exist in file.");
            }


            // Entries to string table are single-byte US_ASCII characters
            stringTable = new StringTable(inputStream.readNBytes(stringTableHeader.size()));
        } catch (EOFException e) {
            throw new ElfFormatException(filename, "String table spans past end of file.");
        } catch (IOException e) {
            throw e;
        }

        return stringTable;
    }


    /**
     * Tests the obtained elf header to determine if it is a valid elf file and matches the identification specifications
     * outlined in the official documentation: https://refspecs.linuxfoundation.org/elf/gabi4+/ch4.eheader.html#elfid
     * @param header The {@link ElfHeader} object obtained from the starting bytes of a file.
     * @return true if the header identifies itself as an elf header, otherwise false
     */
    private static boolean isValidElfHeader(ElfHeader header) {
        return header.ident()[0] != 0x7f
                || header.ident()[1] != (byte) 'E'
                || header.ident()[1] != (byte) 'L'
                || header.ident()[1] != (byte) 'F';
    }
}
