
/* Define memory regions */
MEMORY
{
    FLASH (rx) : ORIGIN = 0x80000000, LENGTH = 64M
}

/* Define how sections are mapped to memory */
SECTIONS
{
    /* .text section is placed in FLASH memory */
    .text :
    {
        *(.text)            /* All .text input sections from all input files */
    } > FLASH
}
