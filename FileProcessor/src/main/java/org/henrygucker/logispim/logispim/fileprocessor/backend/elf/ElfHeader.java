package org.henrygucker.logispim.logispim.fileprocessor.backend.elf;

import java.nio.charset.StandardCharsets;

// This is a record for the 32-bit elf header from the relevant documentation:
// https://refspecs.linuxfoundation.org/elf/gabi4+/ch4.eheader.html
//
// 32-bit ELF Header
//typedef struct {
//        unsigned char   e_ident[EI_NIDENT];
//        Elf32_Half      e_type;
//        Elf32_Half      e_machine;
//        Elf32_Word      e_version;
//        Elf32_Addr      e_entry;
//        Elf32_Off       e_phoff;
//        Elf32_Off       e_shoff;
//        Elf32_Word      e_flags;
//        Elf32_Half      e_ehsize;
//        Elf32_Half      e_phentsize;
//        Elf32_Half      e_phnum;
//        Elf32_Half      e_shentsize;
//        Elf32_Half      e_shnum;
//        Elf32_Half      e_shstrndx;
//} Elf32_Ehdr;
public record ElfHeader(
    byte[] ident,
    short type,
    short machine,
    int version,
    int entry,
    int phoff,
    int shoff,
    int flags,
    short ehsize,
    short phentsize,
    short phnum,
    short shentsize,
    short shnum,
    short shstrndx
) {

    /**
     * Fetches the byte size of the identifier
     * @return int value containing the size, in bytes, of the identifier field
     */
    public static int getIdentifierSize() {
        return 16;
    }
}

// TODO: Create enums for the types, machines,
