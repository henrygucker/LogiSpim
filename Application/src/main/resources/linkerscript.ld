/* Define memory regions */
MEMORY
{
    FLASH (rx) : ORIGIN = 0x00400000, LENGTH = 64M
    RAM (rwx) : ORIGIN = 0x10000000, LENGTH = 64M
}

/* Define how sections are mapped to memory */
SECTIONS
{
    /* .text section is placed in FLASH memory */
    .text :
    {
        *(.text)            /* All .text input sections from all input files */
    } > FLASH

    /* .data section is placed in SRAM memory */
    .data :
    {
        *(.data)            /* All .data (initialized data) input sections */
        *(.rodata)          /* All .rodata (read-only data) input sections */
    } > RAM
}
