import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public enum MainDecoderMaster {
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

    MainDecoderMaster(String name, Integer bitWidth) {
        this.name = name;
        this.bitWidth = bitWidth;
    }


    /**
     * All outputs based on OP code. Displays general case for R-Type w/OP code 0.
     * Index in this array corresponds to OP code. null values are yet to be implemented or are not defined.
     */
    public static final String[][] outputsByOpCode = new String[][]{
            // <Output Fields> Name
            new String[]{"1", "00", "0000", "00", "1000", "0", "00", "1", "R-Type"}, // Op: 00 | R-Type
            null, // SKIP OP 01
            new String[]{"0", "00", "0000", "10", "0000", "0", "00", "0", "j"}, // Op: 02 | j
            new String[]{"1", "11", "0000", "10", "0000", "0", "00", "0", "jal"}, // Op: 03 | jal
            new String[]{"0", "00", "0000", "01", "0000", "0", "00", "0", "beq"}, // Op: 04 | beq
            new String[]{"0", "00", "0000", "01", "0000", "0", "00", "0", "bne"}, // Op: 05 | bne
            new String[]{"0", "00", "0000", "11", "0000", "0", "00", "0", "blez"}, // Op: 06 | blez
            new String[]{"0", "00", "0000", "11", "0000", "0", "00", "0", "bgtz"}, // Op: 07 | bgtz
            new String[]{"1", "00", "0000", "00", "0000", "0", "01", "0", "addi"}, // Op: 08 | addi
            new String[]{"1", "00", "0000", "00", "0001", "0", "01", "0", "addiu"}, // Op: 09 | addiu
            new String[]{"1", "00", "0000", "00", "0011", "0", "01", "0", "slti"}, // Op: 10 | slti
            new String[]{"1", "00", "0000", "00", "0100", "0", "01", "0", "sltiu"}, // Op: 11 | sltiu
            new String[]{"1", "00", "0000", "00", "0101", "0", "10", "0", "andi"}, // Op: 12 | andi
            new String[]{"1", "00", "0000", "00", "0110", "0", "10", "0", "ori"}, // Op: 13 | ori
            new String[]{"1", "00", "0000", "00", "0111", "0", "10", "0", "xori"}, // Op: 14 | xori
            new String[]{"1", "10", "0000", "00", "0000", "0", "00", "0", "lui"}, // Op: 15 | lui
            null, // SKIP OP 16
            null, // SKIP OP 17 (F-Type)
            null, // SKIP OP 18
            null, // SKIP OP 19
            null, // Op: 20 | Custom Instruction for Suspending Processor (no testcase)
            null, // Op: 21 | Custom Instruction for Printing Char (no testcase)
            null, // SKIP OP 22
            null, // SKIP OP 23
            null, // SKIP OP 24
            null, // SKIP OP 25
            null, // SKIP OP 26
            null, // SKIP OP 27
            null, // SKIP OP 28
            null, // SKIP OP 29
            null, // SKIP OP 30
            null, // SKIP OP 31
            new String[]{"1", "01", "0000", "00", "0000", "0", "01", "0", "lb"}, // Op: 32 | lb
            new String[]{"1", "01", "0001", "00", "0000", "0", "01", "0", "lh"}, // Op: 33 | lh
            new String[]{"1", "01", "0110", "00", "0000", "0", "01", "0", "lwl"}, // Op: 34 | lwl
            new String[]{"1", "01", "0010", "00", "0000", "0", "01", "0", "lw"}, // Op: 35 | lw
            new String[]{"1", "01", "0100", "00", "0000", "0", "01", "0", "lbu"}, // Op: 36 | lbu
            new String[]{"1", "01", "0101", "00", "0000", "0", "01", "0", "lhu"}, // Op: 37 | lhu
            new String[]{"1", "01", "0111", "00", "0000", "0", "01", "0", "lwr"}, // Op: 38 | lwr
            null, // SKIP OP 39
            new String[]{"0", "00", "1000", "00", "0000", "0", "01", "0", "sb"}, // Op: 40 | sb
            new String[]{"0", "00", "1001", "00", "0000", "0", "01", "0", "sh"}, // Op: 41 | sh
            new String[]{"0", "00", "1110", "00", "0000", "0", "01", "0", "swl"}, // Op: 42 | swl
            new String[]{"0", "00", "1010", "00", "0000", "0", "01", "0", "sw"}, // Op: 43 | sw
            null, // SKIP OP 44
            null, // SKIP OP 45
            new String[]{"0", "00", "1111", "00", "0000", "0", "01", "0", "swr"}, // Op: 46 | swr
            new String[]{" ", "  ", "    ", "  ", "    ", " ", "  ", " ", "cache"}, // Op: 47 | cache (Not Yet Implemented)
            new String[]{" ", "  ", "    ", "  ", "    ", " ", "  ", " ", "ll"}, // Op: 48 | ll (Not Yet Implemented)
            new String[]{" ", "  ", "    ", "  ", "    ", " ", "  ", " ", "lwc1"}, // Op: 49 | lwc1 (Not Yet Implemented)
            new String[]{" ", "  ", "    ", "  ", "    ", " ", "  ", " ", "lwc2"}, // Op: 50 | lwc2 (Not Yet Implemented)
            new String[]{" ", "  ", "    ", "  ", "    ", " ", "  ", " ", "pref"}, // Op: 51 | pref (Not Yet Implemented)
            null, // SKIP OP 52
            new String[]{" ", "  ", "    ", "  ", "    ", " ", "  ", " ", "ldc1"}, // Op: 53 | ldc1 (Not Yet Implemented)
            new String[]{" ", "  ", "    ", "  ", "    ", " ", "  ", " ", "ldc2"}, // Op: 54 | ldc2 (Not Yet Implemented)
            null, // SKIP OP 55
            new String[]{" ", "  ", "    ", "  ", "    ", " ", "  ", " ", "sc"}, // Op: 56 | sc (Not Yet Implemented)
            new String[]{" ", "  ", "    ", "  ", "    ", " ", "  ", " ", "swc1"}, // Op: 57 | swc1 (Not Yet Implemented)
            new String[]{" ", "  ", "    ", "  ", "    ", " ", "  ", " ", "swc2"}, // Op: 58 | swc2 (Not Yet Implemented)
            null, // SKIP OP 59
            null, // SKIP OP 60
            new String[]{" ", "  ", "    ", "  ", "    ", " ", "  ", " ", "sdc1"}, // Op: 61 | sdc1 (Not Yet Implemented)
            new String[]{" ", "  ", "    ", "  ", "    ", " ", "  ", " ", "sdc2"}, // Op: 62 | sdc2 (Not Yet Implemented)
            null // SKIP OP 63
    };

    /**
     * All overridden R-Type instructions based on Funct value
     */
    public static final String[][] RTypeOverrideCases = new String[][]{
            // OP Funct <Output Fields> Name
            new String[]{"000000", "0000xx", "1", "00", "0000", "00", "1000", "1", "00", "1", "sll/srl/sra"},
            new String[]{"000000", "001000", "0", "00", "0000", "10", "0000", "0", "00", "0", "jr"},
            new String[]{"000000", "001001", "1", "11", "0000", "10", "0000", "0", "00", "0", "jral"},
            new String[]{"000000", "001100", "1", "11", "0000", "00", "0000", "0", "00", "0", "syscall"}
    };

    /**
     * All possible Funct values with Op Code 0 for non-overridden R-Type instructions
     */
    public static final String[][] RTypeNonOverrideCases = new String[][]{
            // OP Funct <Output Fields>
            new String[]{"000000", "000100", "1", "00", "0000", "00", "1000", "0", "00", "1"},
            new String[]{"000000", "000101", "1", "00", "0000", "00", "1000", "0", "00", "1"},
            new String[]{"000000", "000110", "1", "00", "0000", "00", "1000", "0", "00", "1"},
            new String[]{"000000", "000111", "1", "00", "0000", "00", "1000", "0", "00", "1"},
            new String[]{"000000", "001010", "1", "00", "0000", "00", "1000", "0", "00", "1"},
            new String[]{"000000", "001011", "1", "00", "0000", "00", "1000", "0", "00", "1"},
            new String[]{"000000", "001101", "1", "00", "0000", "00", "1000", "0", "00", "1"},
            new String[]{"000000", "001110", "1", "00", "0000", "00", "1000", "0", "00", "1"},
            new String[]{"000000", "001111", "1", "00", "0000", "00", "1000", "0", "00", "1"},
            new String[]{"000000", "010000", "1", "00", "0000", "00", "1000", "0", "00", "1"},
            new String[]{"000000", "010001", "1", "00", "0000", "00", "1000", "0", "00", "1"},
            new String[]{"000000", "010010", "1", "00", "0000", "00", "1000", "0", "00", "1"},
            new String[]{"000000", "010011", "1", "00", "0000", "00", "1000", "0", "00", "1"},
            new String[]{"000000", "010100", "1", "00", "0000", "00", "1000", "0", "00", "1"},
            new String[]{"000000", "010101", "1", "00", "0000", "00", "1000", "0", "00", "1"},
            new String[]{"000000", "010110", "1", "00", "0000", "00", "1000", "0", "00", "1"},
            new String[]{"000000", "010111", "1", "00", "0000", "00", "1000", "0", "00", "1"},
            new String[]{"000000", "011000", "1", "00", "0000", "00", "1000", "0", "00", "1"},
            new String[]{"000000", "011001", "1", "00", "0000", "00", "1000", "0", "00", "1"},
            new String[]{"000000", "011010", "1", "00", "0000", "00", "1000", "0", "00", "1"},
            new String[]{"000000", "011011", "1", "00", "0000", "00", "1000", "0", "00", "1"},
            new String[]{"000000", "011100", "1", "00", "0000", "00", "1000", "0", "00", "1"},
            new String[]{"000000", "011101", "1", "00", "0000", "00", "1000", "0", "00", "1"},
            new String[]{"000000", "011110", "1", "00", "0000", "00", "1000", "0", "00", "1"},
            new String[]{"000000", "011111", "1", "00", "0000", "00", "1000", "0", "00", "1"},
            new String[]{"000000", "100000", "1", "00", "0000", "00", "1000", "0", "00", "1"},
            new String[]{"000000", "100001", "1", "00", "0000", "00", "1000", "0", "00", "1"},
            new String[]{"000000", "100010", "1", "00", "0000", "00", "1000", "0", "00", "1"},
            new String[]{"000000", "100011", "1", "00", "0000", "00", "1000", "0", "00", "1"},
            new String[]{"000000", "100100", "1", "00", "0000", "00", "1000", "0", "00", "1"},
            new String[]{"000000", "100101", "1", "00", "0000", "00", "1000", "0", "00", "1"},
            new String[]{"000000", "100110", "1", "00", "0000", "00", "1000", "0", "00", "1"},
            new String[]{"000000", "100111", "1", "00", "0000", "00", "1000", "0", "00", "1"},
            new String[]{"000000", "101000", "1", "00", "0000", "00", "1000", "0", "00", "1"},
            new String[]{"000000", "101001", "1", "00", "0000", "00", "1000", "0", "00", "1"},
            new String[]{"000000", "101010", "1", "00", "0000", "00", "1000", "0", "00", "1"},
            new String[]{"000000", "101011", "1", "00", "0000", "00", "1000", "0", "00", "1"},
            new String[]{"000000", "101100", "1", "00", "0000", "00", "1000", "0", "00", "1"},
            new String[]{"000000", "101101", "1", "00", "0000", "00", "1000", "0", "00", "1"},
            new String[]{"000000", "101110", "1", "00", "0000", "00", "1000", "0", "00", "1"},
            new String[]{"000000", "101111", "1", "00", "0000", "00", "1000", "0", "00", "1"},
            new String[]{"000000", "110000", "1", "00", "0000", "00", "1000", "0", "00", "1"},
            new String[]{"000000", "110001", "1", "00", "0000", "00", "1000", "0", "00", "1"},
            new String[]{"000000", "110010", "1", "00", "0000", "00", "1000", "0", "00", "1"},
            new String[]{"000000", "110011", "1", "00", "0000", "00", "1000", "0", "00", "1"},
            new String[]{"000000", "110100", "1", "00", "0000", "00", "1000", "0", "00", "1"},
            new String[]{"000000", "110101", "1", "00", "0000", "00", "1000", "0", "00", "1"},
            new String[]{"000000", "110110", "1", "00", "0000", "00", "1000", "0", "00", "1"},
            new String[]{"000000", "110111", "1", "00", "0000", "00", "1000", "0", "00", "1"},
            new String[]{"000000", "111000", "1", "00", "0000", "00", "1000", "0", "00", "1"},
            new String[]{"000000", "111001", "1", "00", "0000", "00", "1000", "0", "00", "1"},
            new String[]{"000000", "111010", "1", "00", "0000", "00", "1000", "0", "00", "1"},
            new String[]{"000000", "111011", "1", "00", "0000", "00", "1000", "0", "00", "1"},
            new String[]{"000000", "111100", "1", "00", "0000", "00", "1000", "0", "00", "1"},
            new String[]{"000000", "111101", "1", "00", "0000", "00", "1000", "0", "00", "1"},
            new String[]{"000000", "111110", "1", "00", "0000", "00", "1000", "0", "00", "1"},
            new String[]{"000000", "111111", "1", "00", "0000", "00", "1000", "0", "00", "1"}
    };

    private static final Path DEFAULT_TEST_VECTOR_PATH = Paths.get(System.getProperty("user.home") + File.separator + "LogiSpim" + File.separator + "test_vectors" + File.separator + "main_decoder.txt");

    /**
     * Prints Markdown tables for MainDecoder outputs & can write MainDecoder test vector to a file specified by a commandline argument.
     * @param args If an argument is passed in, it will be used as a filepath to the location at which the test vector will be written.
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        System.out.println("Outputs By Op Code Markdown Table:");
        System.out.print(MainDecoderMarkdownTableGenerator.getMarkdownTableStringByOpCode());

        System.out.println("\n\nR-Type Overrides:");
        System.out.print(MainDecoderMarkdownTableGenerator.getMarkdownTableStringRTypeOverrides());
        System.out.println("\n\n");

        if (args.length < 1 || !args[0].equals("--write-test-vector")) {
            System.err.println("To write the test vector for the MainDecoder, use the \"--write-test-vector\" commandline flag.");
            return;
        }

        writeTestVector(DEFAULT_TEST_VECTOR_PATH);
        System.out.println("Test vector successfully written to \"" + DEFAULT_TEST_VECTOR_PATH.toAbsolutePath().toString() + "\".");
    }

    static void writeTestVector(Path filepath) throws IOException {
        if (filepath.getParent() != null)
            Files.createDirectories(filepath.getParent());

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filepath.toFile()))) {
            writer.write("# MainDecoder Tests\n");
            writer.write("Op[6] Funct[6] RegWrite[1] RegWriteDataSrc[2] MemOp[4] BranchOp[2] ALUOp[4] ALUSrcA[1] ALUSrcB[2] WriteRegSrc[1]\n");

            // Testcases for varying Op Codes
            for (int i = 1; i < MainDecoderMaster.outputsByOpCode.length; i++) { // Skips index 0 (general R-Type) since that is only for the table printing
                String[] testcase = MainDecoderMaster.outputsByOpCode[i];
                // Skips writing testcases for nonexistent & non-implemented instructions
                if (testcase == null || testcase[0].startsWith(" ")) continue;

                if (testcase.length < MainDecoderMaster.values().length || testcase.length > 1 + MainDecoderMaster.values().length)
                    throw new IllegalStateException("Invalid amount of testcase values.");

                writer.write(String.format("%6s", Integer.toBinaryString(i)).replaceAll(" ", "0"));
                writer.write(" xxxxxx");
                for (int j = 0; j < testcase.length - 1; j++) {
                    writer.write(' ');
                    writer.write(testcase[j]);
                }
                writer.write('\n');
            }

            // Testcases for R-Type Overrides
            for (int i = 0; i < MainDecoderMaster.RTypeOverrideCases.length; i++) {
                String[] testcase = MainDecoderMaster.RTypeOverrideCases[i];
                if (testcase.length < 2 + MainDecoderMaster.values().length || testcase.length > 3 + MainDecoderMaster.values().length)
                    throw new IllegalStateException("Invalid amount of testcase values.");

                for (int j = 0; j < testcase.length - 1; j++) {
                    writer.write(testcase[j]);
                    writer.write(' ');
                }
                writer.write('\n');
            }

            // Testcases for non-overridden R-Type Funct permutations
            for (int i = 0; i < MainDecoderMaster.RTypeNonOverrideCases.length; i++) {
                String[] testcase = MainDecoderMaster.RTypeNonOverrideCases[i];
                if (testcase.length < 2 + MainDecoderMaster.values().length || testcase.length > 3 + MainDecoderMaster.values().length)
                    throw new IllegalStateException("Invalid amount of testcase values.");

                for (String value : testcase) {
                    writer.write(value);
                    writer.write(' ');
                }
                writer.write('\n');
            }
        }
    }
}
