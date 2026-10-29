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

import org.henrygucker.logispim.fileprocessor.backend.fileutils.ResourceFileUtil;

import static org.junit.jupiter.api.Assertions.*;

public class DocsTests {
   static final Path DOCS_DIR = Paths.get(System.getProperty("user.dir")).getParent().resolve("docs");

   static final String MAIN_DECODER_MARKDOWN_RESOURCE = "01_main_decoder.md";

    @TempDir(cleanup = CleanupMode.ALWAYS)
    static Path tempDir;
    static Path mainDecoderMarkdownPath;

    @BeforeAll
    static void init() throws IOException, InterruptedException {
        mainDecoderMarkdownPath = tempDir.resolve(MAIN_DECODER_MARKDOWN_RESOURCE);
        ResourceFileUtil.copyResourceToLocation(MAIN_DECODER_MARKDOWN_RESOURCE, mainDecoderMarkdownPath);
    }

    @Test
    void mainDecoderTableTests() {
        String markdownContent;
        try {
            markdownContent = FileUtils.readFileToString(mainDecoderMarkdownPath.toFile(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            fail("Main Decoder docs file does not exist. Make sure the resource file has the correct name and the correct" +
                    "path targeted within the Testing module's pom.xml file.");
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
