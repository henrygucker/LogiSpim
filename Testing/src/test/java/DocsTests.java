import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

public class DocsTests {
   static final Path DOCS_DIR = Paths.get(System.getProperty("user.dir")).getParent().resolve("docs");

   static final Path MAIN_DECODER_MARKDOWN = DOCS_DIR.resolve("src" + File.separator + "05_core_components" + File.separator + "5_3_1_main_decoder.md");

    @Test
    void mainDecoderTableTests() {
        String markdownContent;
        try {
            markdownContent = FileUtils.readFileToString(MAIN_DECODER_MARKDOWN.toFile(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            fail("Main Decoder docs file does not exist.");
            return;
        }

        String outputsByOpCode = MainDecoderMarkdownTableGenerator.getMarkdownTableStringByOpCode().replaceAll("\\s+", "").replaceAll("-+", "-");
        String RTypeOverriddenOutputs = MainDecoderMarkdownTableGenerator.getMarkdownTableStringRTypeOverrides().replaceAll("\\s+", "").replaceAll("-+", "-");

        System.out.println(markdownContent);

        markdownContent = markdownContent.replaceAll("\\s+", "").replaceAll("-+", "-");

        // Asserts that markdownContent must contain both tables provided by MainDecoderMarkdownTableGenerator
        assert(markdownContent.contains(outputsByOpCode));
        assert(markdownContent.contains(RTypeOverriddenOutputs));
    }
}
