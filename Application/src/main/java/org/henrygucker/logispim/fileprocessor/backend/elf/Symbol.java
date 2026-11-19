package org.henrygucker.logispim.fileprocessor.backend.elf;

// This is a record for the 32-bit elf symbol entity found in the symbol table from the relevant documentation:
// https://refspecs.linuxbase.org/elf/gabi4+/ch4.symtab.html

// 32-Bit Elf Symbol in Symbol Table
//typedef struct {
//	Elf32_Word	st_name;
//	Elf32_Addr	st_value;
//	Elf32_Word	st_size;
//	unsigned char	st_info;
//	unsigned char	st_other;
//	Elf32_Half	st_shndx;
//} Elf32_Sym;
public record Symbol(
        int name,
        int value,
        int size,
        byte info,
        byte other,
        short shndx
) {
}
