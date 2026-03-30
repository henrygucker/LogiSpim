package org.henrygucker.logispim.testvectorgenerator;

public enum MainDecoderOutputs {
    REG_WRITE("RegWrite", 1),
    REG_WRITE_DATA_SRC("RegWriteDataSrc", 2),
    MEM_OP("MemOp", 4),
    BRANCH_OP("BranchOp", 2),
    ALU_OP("ALUOp", 4),
    ALU_SRC_A("ALUSrcA", 1),
    ALU_SRC_B("ALUSrcB", 2),
    WRITE_REG_SRC("WriteRegSrc", 1);

    public final String name;
    public final Integer bitWidth;

    MainDecoderOutputs(String name, Integer bitWidth) {
        this.name = name;
        this.bitWidth = bitWidth;
    }
}
