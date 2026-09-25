package jp.nw.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AttachmentFileValidatorTest {
    @TempDir
    Path directory;

    private final AttachmentFileValidator validator = new AttachmentFileValidator(1024);

    @Test
    void acceptsUtf8TextAndUsesCanonicalMimeType() throws Exception {
        Path file = write("正常なテキストです。".getBytes(StandardCharsets.UTF_8));

        var result = validator.validate("report.txt", "application/octet-stream", Files.size(file), file);

        assertEquals("report.txt", result.originalName());
        assertEquals("text/plain", result.contentType());
    }

    @Test
    void rejectsExecutableExtension() throws Exception {
        Path file = write("not executable".getBytes(StandardCharsets.UTF_8));

        assertThrows(IllegalArgumentException.class,
                () -> validator.validate("malware.exe", "application/octet-stream", Files.size(file), file));
    }

    @Test
    void rejectsExtensionSpoofing() throws Exception {
        Path file = write("this is not a PDF".getBytes(StandardCharsets.UTF_8));

        assertThrows(IllegalArgumentException.class,
                () -> validator.validate("spoofed.pdf", "application/pdf", Files.size(file), file));
    }

    @Test
    void rejectsMimeTypeMismatch() throws Exception {
        Path file = write("plain text".getBytes(StandardCharsets.UTF_8));

        assertThrows(IllegalArgumentException.class,
                () -> validator.validate("data.txt", "text/html", Files.size(file), file));
    }

    @Test
    void stripsClientPathButRejectsDangerousResultingName() throws Exception {
        Path file = write("plain text".getBytes(StandardCharsets.UTF_8));

        var result = validator.validate("C:\\fakepath\\safe.txt", "text/plain", Files.size(file), file);
        assertEquals("safe.txt", result.originalName());
        assertThrows(IllegalArgumentException.class,
                () -> validator.validate("C:\\fakepath\\.hidden.txt", "text/plain", Files.size(file), file));
    }

    @Test
    void rejectsDeclaredSizeMismatchAndOversizedFile() throws Exception {
        Path small = write("text".getBytes(StandardCharsets.UTF_8));
        assertThrows(IllegalArgumentException.class,
                () -> validator.validate("data.txt", "text/plain", Files.size(small) + 1, small));

        Path large = write(new byte[1025]);
        assertThrows(IllegalArgumentException.class,
                () -> validator.validate("large.txt", "text/plain", Files.size(large), large));
    }

    private Path write(byte[] bytes) throws Exception {
        Path file = Files.createTempFile(directory, "attachment-", ".tmp");
        Files.write(file, bytes);
        return file;
    }
}
