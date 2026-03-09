
#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <stdint.h>


// Common ELF identification bytes
#define EI_NIDENT 16

#define TEXT_SEGMENT_SIZE 100
#define DATA_SEGMENT_SIZE 0x4000

// 32-bit ELF Header
typedef struct {
    unsigned char e_ident[EI_NIDENT]; /* ELF identification */
    uint16_t      e_type;           /* Object file type */
    uint16_t      e_machine;        /* Machine type */
    uint32_t      e_version;        /* Object file version */
    uint32_t      e_entry;          /* Entry point address */
    uint32_t      e_phoff;          /* Program header table file offset */
    uint32_t      e_shoff;          /* Section header table file offset */
    uint32_t      e_flags;          /* Processor-specific flags */
    uint16_t      e_ehsize;         /* ELF header size in bytes */
    uint16_t      e_phentsize;      /* Program header table entry size */
    uint16_t      e_phnum;          /* Program header table entry count */
    uint16_t      e_shentsize;      /* Section header table entry size */
    uint16_t      e_shnum;          /* Section header table entry count */
    uint16_t      e_shstrndx;       /* Section header string table index */
} Elf32_Ehdr;

typedef struct {
    uint32_t sh_name;      /* Section name (index into string table) */
    uint32_t sh_type;      /* Section type */
    uint32_t sh_flags;     /* Section flags */
    uint32_t sh_addr;      /* Address in memory */
    uint32_t  sh_offset;    /* Offset in file */
    uint32_t sh_size;      /* Size of section */
    uint32_t sh_link;      /* Link to another section */
    uint32_t sh_info;      /* Additional section information */
    uint32_t sh_addralign; /* Section alignment */
    uint32_t sh_entsize;   /* Size of entry if section holds table */
} Elf32_Shdr;

typedef struct {
	uint32_t	st_name;
	uint32_t	st_value;
	uint32_t	st_size;
	uint8_t	    st_info;
	uint8_t     st_other;
	uint16_t	st_shndx;
} Elf32_Sym;


void create_temp_dir() {
    const char *dirname = "bins";
    mode_t mode = 0755;

    mkdir(dirname, mode);

    const char *innerdirname = "bins/temp";

    mkdir(innerdirname, mode);
}

void create_elf_file(char *asm_filename) {
    char command1[256];

    // Prevents strncat from using old data
    for (int i = 0; i < 256; i++) {
        command1[i] = 0;
    }

    char command1prefix[] = "mipsel-unknown-linux-gnu-as -EL -g -msoft-float -O0 -o bins/temp/temp.o ";

    strncat(command1, command1prefix, 256);
    strncat(command1, asm_filename, 256);
    system(command1);

    
    system("mipsel-unknown-linux-gnu-ld -EL -T src/linkerscript-kernel-text.ld bins/temp/temp.o -o bins/temp/temp.elf");
}

char *get_string_table(Elf32_Ehdr *header, Elf32_Shdr *sections, char *elf_filename) {
    FILE *string_table_reader;
    string_table_reader = fopen(elf_filename, "rb");
    if (string_table_reader == NULL) {
        printf("Error opening program file for reading of the string table.\n");
        return NULL;
    }
    
    fseek(string_table_reader, sections[header->e_shstrndx].sh_offset, SEEK_SET);

    char *str_table;
    str_table = (char *) malloc(1 * sections[header->e_shstrndx].sh_size);

    // Reads in string table
    fread(str_table, 1, sections[header->e_shstrndx].sh_size, string_table_reader);
    fclose(string_table_reader);

    return str_table;
}

Elf32_Shdr *populate_elf_header_and_get_section_header_arr(Elf32_Ehdr *header, char *elf_filename) {
    FILE *elf;
    elf = fopen("bins/temp/temp.elf", "rb");
    if (elf == NULL) {
        printf("Error opening program file.\n");
        return NULL;
    }

    // Reads in header
    fread(header, sizeof(Elf32_Ehdr), 1, elf);

    // Offset to start of section header table
    fseek(elf, header->e_shoff, SEEK_SET);

    Elf32_Shdr *sections;
    sections = (Elf32_Shdr *) malloc(header->e_shnum * sizeof(Elf32_Shdr));

    for (int i = 0; i < header->e_shnum; i++) {
        fread(&sections[i], sizeof(Elf32_Shdr), 1, elf);
    }
    fclose(elf);

    return sections;
}

Elf32_Shdr *get_section_header(Elf32_Ehdr *header, Elf32_Shdr *sections, char *str_table, char *section_name) {
    Elf32_Shdr *section = NULL;
    for (int i = 0; i < header->e_shnum; i++) {
        if (strcmp(&str_table[sections[i].sh_name], section_name) == 0) {
            section = &sections[i];
        }
    }

    if (section == NULL) {
        // Returns NULL if section does not exist
        printf("Section \"%s\" not found.\n", section_name);
        return NULL;
    }

    return section;
}

uint8_t *get_section_binary(Elf32_Ehdr *header, Elf32_Shdr *sections, char* str_table, char *elf_filename, char *section_name) {
    Elf32_Shdr *section = get_section_header(header, sections, str_table, section_name);

    if (section == NULL)
        return NULL;

    uint8_t *bin;
    bin = (uint8_t *) malloc(section->sh_size);

    FILE *reader;
    reader = fopen(elf_filename, "rb");

    fseek(reader, section->sh_offset, SEEK_SET);

    fread(bin, 1, section->sh_size, reader);

    fclose(reader);

    return bin;
}

int main(int argc, char *argv[]) {
    if (argc != 2) {
        printf("Exactly one argument must be passed into the program.\n");
        return 1;
    }

    create_temp_dir();

    // Creates elf file at out/program.elf
    create_elf_file(argv[1]);


    // Gets elf header and section header table
    Elf32_Ehdr header;
    Elf32_Shdr *sections = populate_elf_header_and_get_section_header_arr(&header, "bins/temp/temp.elf");
    if (sections == NULL)
        return 1;

    // Gets string table
    char *str_table = get_string_table(&header, sections, "bins/temp/temp.elf");
    if (str_table == NULL) {
        free(sections);
        return 1;
    }

    Elf32_Sym  *symtab;
    Elf32_Shdr *symtab_shdr = get_section_header(&header, sections, str_table, ".symtab");
    symtab = (Elf32_Sym *) get_section_binary(&header, sections, str_table, "bins/temp/temp.elf", ".symtab");
    if (symtab_shdr == NULL || symtab == NULL) {
        free(sections);
        free(str_table);
        return 1;
    }


    Elf32_Shdr *text_shdr = get_section_header(&header, sections, str_table, ".text");
    if (text_shdr == NULL) {
        free(sections);
        free(str_table);
        free(symtab);
        return 1;
    }

    Elf32_Shdr *sym_str_table_header = get_section_header(&header, sections, str_table, ".strtab");
    char *sym_str_table = (char *)get_section_binary(&header, sections, str_table, "bins/temp/temp.elf", ".strtab");

    // Gets byte offsets for exit and print_char custom instructions in the text segment
    int exit_offset_in_text = -1;
    int print_char_offset_in_text = -1;
    for (int i = 0; i < symtab_shdr->sh_size / symtab_shdr->sh_entsize; i++) {
        if (strcmp(&sym_str_table[symtab[i].st_name], "exit") == 0) {
            exit_offset_in_text = symtab[i].st_value - text_shdr->sh_addr;
        }
        if (strcmp(&sym_str_table[symtab[i].st_name], "print_char") == 0) {
            print_char_offset_in_text = symtab[i].st_value - text_shdr->sh_addr;
        }
    }

    if (exit_offset_in_text == -1) {
        printf("No \"exit\" label found in %s\n", argv[1]);
        free(sections);
        free(str_table);
        free(symtab);
        return 1;
    }

    if (print_char_offset_in_text == -1) {
        printf("No \"print_char\" label found in %s\n", argv[1]);
        free(sections);
        free(str_table);
        free(symtab);
        return 1;
    }

    // Gets binary version of text section
    uint8_t *text = get_section_binary(&header, sections, str_table, "bins/temp/temp.elf", ".text");
    if (text == NULL) {
        printf("There was an issue while retreiving the text section binary.\n");
        free(sections);
        free(str_table);
        free(symtab);
        free(text);
        return 1;
    }

    // Overwrites nop instructions with custom instructions
    uint8_t exit_little_endian[] = {0x00, 0x00, 0x00, 0x50};
    uint8_t print_char_little_endian[] = {0x00, 0x00, 0x00, 0x54};
    for (int i = 0; i < 4; i++) {
        text[exit_offset_in_text + i] = exit_little_endian[i];
        text[print_char_offset_in_text + i] = print_char_little_endian[i];
    }

    // Writing to text file
    FILE *textfile;

    // create_bins_dir();

    textfile = fopen("bins/kernel_text", "wb");

    if (textfile == NULL) {
        printf("Error creating kernel_text file.\n");
        free(sections);
        free(str_table);
        free(symtab);
        free(text);
        return 1;
    }

    fwrite(text, 1, text_shdr->sh_size, textfile);
    printf("Write to bins/kernel_text complete!\n");

    fclose(textfile);





    free(sections);
    free(str_table);
    free(symtab);
    free(text);

    printf("\nCompilation complete! Access file(s) in the ./bins directory.\n");

    return 0;
}
