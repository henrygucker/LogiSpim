package org.henrygucker.logimips.testprogramgenerator;

public record LogiSpimTest(int num, String testInstruction, int[] primaryTestValues, int[] secondaryTestValues, int[] expectedValues) {
    public String getTestContentDataEntry() {
        StringBuilder builder = new StringBuilder();

        // test_{{NUM}}_num_testcases: .word ...
        builder.append("test_");
        builder.append(num);
        builder.append("_num_testcases: .word ");
        builder.append(primaryTestValues.length);
        builder.append('\n');

        // test_{{NUM}}_primary_test_values: .word ...
        builder.append("test_");
        builder.append(num);
        builder.append("_primary_test_values: .word ");
        for (int i = 0; i < primaryTestValues.length - 1; i++) {
            builder.append(primaryTestValues[i]);
            builder.append(", ");
        }
        builder.append(primaryTestValues[primaryTestValues.length - 1]);
        builder.append('\n');


        // test_{{NUM}}_secondary_test_values: .word ...
        builder.append("test_");
        builder.append(num);
        if (secondaryTestValues == null) {
            // No Secondary Test Values, allocates empty space to avoid segmentation fault
            builder.append("_secondary_test_values: .space ");
            builder.append(primaryTestValues.length * 4);
        } else {
            // Secondary Values Present
            builder.append("_secondary_test_values: .word ");
            for (int i = 0; i < primaryTestValues.length - 1; i++) {
                builder.append(secondaryTestValues[i]);
                builder.append(", ");
            }
            builder.append(secondaryTestValues[primaryTestValues.length - 1]);
        }
        builder.append('\n');

        // test_{{NUM}}_expected_values: .word ...
        builder.append("test_");
        builder.append(num);
        builder.append("_expected_values: .word ");
        for (int i = 0; i < primaryTestValues.length - 1; i++) {
            builder.append(expectedValues[i]);
            builder.append(", ");
        }
        builder.append(expectedValues[primaryTestValues.length - 1]);

        return builder.toString();
    }

    public String getTestInstructionStringDataEntry() {
        return "test_" + num + "_instruction_string: .asciiz \"\\nTest " + num + ":\\n" + testInstruction.replace("\n", "\\n") + "\"";
    }

    public String getTestJumpEntry() {
        return "    jal test_" + num;
    }

    public String getTestInstruction() {
        return "    " + testInstruction;
    }

    public String getNumString() {
        return "" + num;
    }

    public void updateNum(int num) {
    }
}
