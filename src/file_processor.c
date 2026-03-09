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

void create_temp_dir() {
    const char *dirname = "bins";
    mode_t mode = 0755;

    mkdir(dirname, mode);

    const char *innerdirname = "bins/temp";

    mkdir(innerdirname, mode);
}

void src_asm_preprocessor(char *asm_filename) {
    
    FILE *asm_file;
    asm_file = fopen(asm_filename, "rb");

    FILE *asm_processed;
    asm_processed = fopen("bins/temp/src.s", "wb");

    char buff[1024];

    int i = 0;
    int j = 0;
    int num_chars = 0;
    int is_match = 1;

    char *syscall = "syscall";
    char *text_directive = ".text";

    char *move_k0_ra_instruction = "    move $k0, $ra\n";
    char *nop = "   nop\n";
    char *text_wrapper = "_entry:\njal main\nli $v0, 10\nmove $k0, $ra\nsyscall\n";

    while (fgets(buff, 1024, asm_file) != NULL) {
        i = 0;  
        while (buff[i] == ' ' || buff[i] == '\t')
            i++;
        
        j = 0;
        is_match = 1;
        while (syscall[j] != '\0') {
            if (i + j < 1024 && syscall[j] == buff[i + j]) {
                j++;
                continue;
            }
            
            is_match = 0;
            break;
        }

        if (is_match != 0) {
            fwrite(move_k0_ra_instruction, sizeof(char), strlen(move_k0_ra_instruction), asm_processed);
        }
        
        fwrite(buff, sizeof(char), strlen(buff), asm_processed);

        
        if (is_match != 0) {
            fwrite(nop, sizeof(char), strlen(nop), asm_processed);
        }

        j = 0;
        is_match = 1;
        while (text_directive[j] != '\0') {
            if (i + j < 1024 && text_directive[j] == buff[i + j]) {
                j++;
                continue;
            }

            is_match = 0;
            break;
        }

        if (is_match != 0) {
            fwrite(text_wrapper, sizeof(char), strlen(text_wrapper), asm_processed);
        }
    }
    
    fclose(asm_file);
    fclose(asm_processed);

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

    
    system("mipsel-unknown-linux-gnu-ld -EL -T src/linkerscript.ld bins/temp/temp.o -o bins/temp/temp.elf");
}

int main(int argc, char *argv[]) {
    if (argc != 2) {
        printf("Exactly one argument must be passed into the program.\n");
        return 1;
    }

    create_temp_dir();

    src_asm_preprocessor(argv[1]);

    // Creates elf file at out/program.elf
    create_elf_file("bins/temp/src.s");

    FILE *elf;
    elf = fopen("bins/temp/temp.elf", "rb");
    if (elf == NULL) {
        printf("Error opening program file.\n");
        return 1;
    }

    Elf32_Ehdr header;
    fread(&header, sizeof(Elf32_Ehdr), 1, elf);


    fseek(elf, header.e_shoff, SEEK_SET);

    Elf32_Shdr *sections;
    sections = (Elf32_Shdr *) malloc(header.e_shnum * sizeof(Elf32_Shdr));

    for (int i = 0; i < header.e_shnum; i++) {
        fread(&sections[i], sizeof(Elf32_Shdr), 1, elf);
    }
    fclose(elf);

    FILE *string_table_reader;
    string_table_reader = fopen("bins/temp/temp.elf", "rb");
    if (string_table_reader == NULL) {
        printf("Error opening program file for reading of the string table.\n");
        return 1;
    }
    
    fseek(string_table_reader, sections[header.e_shstrndx].sh_offset, SEEK_SET);

    char *str_table;
    str_table = (char *) malloc(1 * sections[header.e_shstrndx].sh_size);

    // Reads in string table
    fread(str_table, 1, sections[header.e_shstrndx].sh_size, string_table_reader);
    fclose(string_table_reader);

    Elf32_Shdr text_section;
    Elf32_Shdr data_section;
    for (int i = 0; i < header.e_shnum; i++) {
        if (strcmp(&str_table[sections[i].sh_name], ".text") == 0) {
            text_section = sections[i];
        } else if (strcmp(&str_table[sections[i].sh_name], ".data") == 0) {
            data_section = sections[i];
        }
    }

    uint8_t *text;
    uint8_t *data;

    text = (uint8_t *) malloc(text_section.sh_size);
    data = (uint8_t *) malloc(data_section.sh_size);

    FILE *text_reader;
    FILE *data_reader;

    text_reader = fopen("bins/temp/temp.elf", "rb");
    data_reader = fopen("bins/temp/temp.elf", "rb");

    fseek(text_reader, text_section.sh_offset, SEEK_SET);
    fseek(data_reader, data_section.sh_offset, SEEK_SET);

    fread(text, 1, text_section.sh_size, text_reader);
    fread(data, 1, data_section.sh_size, data_reader);

    fclose(text_reader);
    fclose(data_reader);

    // Writing to text and data files
    FILE *textfile;
    FILE *datafile;

    // create_bins_dir();

    textfile = fopen("bins/text", "wb");
    datafile = fopen("bins/data", "wb");

    if (textfile == NULL) {
        printf("Error opening .text file.\n");
        return 1;
    }
    if (datafile == NULL) {
        printf("Error opening .data file.\n");
        return 1;
    }

    printf("Writing .text section to file bins/text\n");
    fwrite(text, 1, text_section.sh_size, textfile);
    printf("Write to bins/text complete!\n");

    printf("Writing .data section to file bins/data\n");
    fwrite(data, 1, data_section.sh_size, datafile);
    printf("Write to bins/data complete!\n");

    fclose(textfile);
    fclose(datafile);


    free(text);
    free(data);
    free(sections);
    free(str_table);

    printf("\nCompilation complete! Access files in the ./bins directory.\n");

    return 0;
}
