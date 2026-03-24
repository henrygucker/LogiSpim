package org.henrygucker.logispim.fileprocessor.backend.elf;


// This is a record for the 32-bit elf program header from the relevant documentation:
// https://refspecs.linuxbase.org/elf/gabi4+/ch5.pheader.html

// Elf-32 Program Header
//typedef struct {
//	Elf32_Word	p_type;
//	Elf32_Off	p_offset;
//	Elf32_Addr	p_vaddr;
//	Elf32_Addr	p_paddr;
//	Elf32_Word	p_filesz;
//	Elf32_Word	p_memsz;
//	Elf32_Word	p_flags;
//	Elf32_Word	p_align;
//} Elf32_Phdr;
public record ProgramHeader(
        int type,
        int offset,
        int vaddr,
        int paddr,
        int filesz,
        int memsz,
        int flags,
        int align
) {
}
