package org.henrygucker.logimips.fileprocessor.backend.elf;

public class StringTable {
    private final String string;

    public StringTable(byte[] rawBytes) {
        StringBuilder stringBuilder = new StringBuilder(rawBytes.length);

        for (byte b : rawBytes) {
            stringBuilder.append((char) b);
        }

        string = stringBuilder.toString();
    }

    public String getString(int index) {
        StringBuilder stringBuilder = new StringBuilder();

        while (index < string.length() && string.charAt(index) != (char) 0) {
            stringBuilder.append(string.charAt(index));

            index++;
        }

        return stringBuilder.toString();
    }

    public String getRawString() {
        return string;
    }
}
