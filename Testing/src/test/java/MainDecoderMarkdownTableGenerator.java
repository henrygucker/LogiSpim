class MainDecoderMarkdownTableGenerator {
    static String getMarkdownTableStringByOpCode() {
        StringBuilder builder = new StringBuilder();

        String[] invalidOpCodeReplacementLine = new String[]{
                " ", "  ", "    ", "  ", "    ", " ", "  ", " ", "N/A"
        };

        builder.append("| # | OP | Instruction |");
        for (int i = 0; i < MainDecoderMaster.values().length; i++) {
            builder.append(" " + (i + 1) + " |");
        }
        builder.append('\n');

        builder.append("|");
        for (int i = 0; i < 3 + MainDecoderMaster.values().length; i++) {
            builder.append(" :---: |");
        }
        builder.append('\n');

        for (int i = 0; i < MainDecoderMaster.outputsByOpCode.length; i++) {
            String[] testcase = MainDecoderMaster.outputsByOpCode[i];

            if (testcase == null)
                testcase = invalidOpCodeReplacementLine;

            builder.append(String.format("| %02d ", i));
            builder.append("| `" + String.format("%6s", Integer.toBinaryString(i)).replaceAll(" ", "0") + "` ");
            builder.append("| `" + testcase[testcase.length - 1] + "` |");

            for (int j = 0; j < MainDecoderMaster.values().length; j++) {
                if (!testcase[j].startsWith(" "))
                    builder.append(" `" + testcase[j] + "` |");
                else
                    builder.append(" |");
            }
            builder.append('\n');
        }

        return builder.toString();
    }

    static String getMarkdownTableStringRTypeOverrides() {
        StringBuilder builder = new StringBuilder();

        final int NUM_DECODER_OUTPUTS = MainDecoderMaster.values().length;
        final int TABLE_COLUMNS = 2 + NUM_DECODER_OUTPUTS;

        builder.append("| Funct | Instruction |");
        for (int i = 0; i < NUM_DECODER_OUTPUTS; i++) {
            builder.append(" " + (i + 1) + " |");
        }
        builder.append('\n');

        builder.append("|");
        for (int i = 0; i < TABLE_COLUMNS; i++) {
            builder.append(" :---: |");
        }
        builder.append('\n');

        for (int i = 0; i < MainDecoderMaster.RTypeOverrideCases.length; i++) {
            String[] testcase = MainDecoderMaster.RTypeOverrideCases[i];

            // Testcases must have exactly one extra value in index 1 which is not used for the table generation
            assert(TABLE_COLUMNS == testcase.length - 1);

            builder.append("|");

            // Funct value
            builder.append(" `" + testcase[1] + "` |");

            // Instruction name
            builder.append(" `" + testcase[testcase.length - 1] + "` |");

            for (int j = 2; j < TABLE_COLUMNS; j++) {
                builder.append(" `" + testcase[j] + "` |");
            }
            builder.append('\n');
        }

        return builder.toString();
    }
}
