import java.io.*;
import java.nio.file.Path;

public class MainDecoderTestsa {

    private static final String markdownFileResourcePath = "docs" + File.separator + "src" + File.separator + "04_core_components" + File.separator + "main_decoder.md";

    public static void main(String[] args) throws IOException {
    }

    private static boolean containsMarkdownTables(Path filepath) {
        String byOpCodeExpected = MainDecoderMarkdownTableGenerator.getMarkdownTableStringByOpCode().replace("\\s+", "");

        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream(markdownFileResourcePath)))) {
            String inn;
            while ((inn = reader.readLine()) != null) {
                builder.append(inn);
                builder.append('\n');
            }
        } catch (IOException e) {
            return false;
        }

        String markdownFileContents = builder.toString().replace("\\s+", "");

        if (!markdownFileContents.contains(byOpCodeExpected))
            return false;

        return true;
    }


}
