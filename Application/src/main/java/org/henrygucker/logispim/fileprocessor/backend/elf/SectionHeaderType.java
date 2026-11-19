package org.henrygucker.logispim.fileprocessor.backend.elf;

public enum SectionHeaderType {
    NULL(0),
    PROGBITS(1),
    SYMTAB(2),
    STRTAB(3),
    RELA(4),
    HASH(5),
    DYNAMIC(6),
    NOTE(7),
    NOBITS(8),
    REL(9),
    SHLIB(10),
    DYNSYM(11),
    INIT_ARRAY(14),
    FINI_ARRAY(15),
    PREINIT_ARRAY(16),
    GROUP(17),
    SYMTAB_SHNDX(18),
    LOOS(0x60000000),
    HIOS(0x6fffffff),
    LOPROC(0x70000000),
    HIPROC(0x7fffffff),
    LOUSER(0x80000000),
    HIUSER(0xffffffff);

    private final int val;

    SectionHeaderType(int val) {
        this.val = val;
    }

    public static SectionHeaderType fromInt(int val) {
        for (SectionHeaderType sht : SectionHeaderType.values()) {
            if (sht.val == val)
                return sht;
        }
//        throw new IllegalArgumentException("0x" + Integer.toHexString(val) + " is not a valid SectionHeaderType.");
        return NULL;
    }
}
