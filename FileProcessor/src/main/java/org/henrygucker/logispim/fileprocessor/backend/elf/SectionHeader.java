package org.henrygucker.logispim.fileprocessor.backend.elf;

// This is a record for the 32-bit elf header from the relevant documentation:
// https://refspecs.linuxbase.org/elf/gabi4+/ch4.sheader.html

// 32-Bit Elf Section Header
//typedef struct {
//	Elf32_Word	sh_name;
//	Elf32_Word	sh_type;
//	Elf32_Word	sh_flags;
//	Elf32_Addr	sh_addr;
//	Elf32_Off	sh_offset;
//	Elf32_Word	sh_size;
//	Elf32_Word	sh_link;
//	Elf32_Word	sh_info;
//	Elf32_Word	sh_addralign;
//	Elf32_Word	sh_entsize;
//} Elf32_Shdr;
public record SectionHeader(
        int name,
        SectionHeaderType type, // TODO: Create the enum for type to distinguish types of section headers
        int flags,
        int addr,
        int offset,
        int size,
        int link,
        int info,
        int addralign,
        int entsize
) {
}
