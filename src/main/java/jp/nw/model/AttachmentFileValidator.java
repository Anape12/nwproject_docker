package jp.nw.model;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class AttachmentFileValidator {
    private static final Map<String, FileType> ALLOWED_TYPES = Map.ofEntries(
            Map.entry("pdf", new FileType("application/pdf", Set.of("application/pdf"), Signature.PDF)),
            Map.entry("png", new FileType("image/png", Set.of("image/png"), Signature.PNG)),
            Map.entry("jpg", new FileType("image/jpeg", Set.of("image/jpeg", "image/jpg"), Signature.JPEG)),
            Map.entry("jpeg", new FileType("image/jpeg", Set.of("image/jpeg", "image/jpg"), Signature.JPEG)),
            Map.entry("gif", new FileType("image/gif", Set.of("image/gif"), Signature.GIF)),
            Map.entry("txt", new FileType("text/plain", Set.of("text/plain"), Signature.TEXT)),
            Map.entry("csv", new FileType("text/csv", Set.of("text/csv", "application/csv", "text/plain"), Signature.TEXT)),
            Map.entry("zip", new FileType("application/zip",
                    Set.of("application/zip", "application/x-zip-compressed"), Signature.ZIP)),
            Map.entry("docx", new FileType(
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                    Set.of("application/vnd.openxmlformats-officedocument.wordprocessingml.document"), Signature.DOCX)),
            Map.entry("xlsx", new FileType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    Set.of("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"), Signature.XLSX)),
            Map.entry("pptx", new FileType(
                    "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                    Set.of("application/vnd.openxmlformats-officedocument.presentationml.presentation"), Signature.PPTX)));

    private final long maximumSize;

    public AttachmentFileValidator(long maximumSize) {
        this.maximumSize = maximumSize;
    }

    public ValidatedFile validate(String submittedName, String submittedContentType,
            long declaredSize, Path file) throws IOException {
        String originalName = sanitizeFileName(submittedName);
        long actualSize = Files.size(file);
        if (actualSize <= 0 || actualSize > maximumSize || actualSize != declaredSize) {
            throw new IllegalArgumentException("ファイルサイズが不正です。10MB以下のファイルを選択してください。");
        }

        String extension = extension(originalName);
        FileType expected = ALLOWED_TYPES.get(extension);
        if (expected == null) {
            throw new IllegalArgumentException("このファイル形式は添付できません。");
        }

        String supplied = normalizeContentType(submittedContentType);
        if (!"application/octet-stream".equals(supplied) && !expected.acceptedContentTypes().contains(supplied)) {
            throw new IllegalArgumentException("拡張子とMIMEタイプが一致しません。");
        }
        if (!matchesSignature(file, expected.signature())) {
            throw new IllegalArgumentException("拡張子とファイル内容が一致しません。");
        }
        return new ValidatedFile(originalName, expected.canonicalContentType(), actualSize);
    }

    private String sanitizeFileName(String submittedName) {
        if (submittedName == null) throw new IllegalArgumentException("ファイル名が不正です。");
        String normalized = submittedName.replace('\\', '/');
        String name = normalized.substring(normalized.lastIndexOf('/') + 1).trim();
        if (name.isBlank() || name.length() > 255 || name.startsWith(".") || name.endsWith(".")
                || name.chars().anyMatch(c -> Character.isISOControl(c) || c == '/' || c == '\\' || c == ':')) {
            throw new IllegalArgumentException("ファイル名が不正です。");
        }
        return name;
    }

    private String extension(String name) {
        int dot = name.lastIndexOf('.');
        if (dot <= 0 || dot == name.length() - 1) throw new IllegalArgumentException("拡張子が必要です。");
        return name.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    private String normalizeContentType(String value) {
        if (value == null || value.isBlank()) return "application/octet-stream";
        return value.split(";", 2)[0].trim().toLowerCase(Locale.ROOT);
    }

    private boolean matchesSignature(Path file, Signature signature) throws IOException {
        return switch (signature) {
            case PDF -> startsWith(file, new byte[] { '%', 'P', 'D', 'F', '-' });
            case PNG -> startsWith(file, new byte[] { (byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A });
            case JPEG -> startsWith(file, new byte[] { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF });
            case GIF -> startsWith(file, "GIF87a".getBytes(StandardCharsets.US_ASCII))
                    || startsWith(file, "GIF89a".getBytes(StandardCharsets.US_ASCII));
            case TEXT -> isUtf8Text(file);
            case ZIP -> isZip(file) && !containsOfficeMarker(file, null);
            case DOCX -> isZip(file) && containsOfficeMarker(file, "word/");
            case XLSX -> isZip(file) && containsOfficeMarker(file, "xl/");
            case PPTX -> isZip(file) && containsOfficeMarker(file, "ppt/");
        };
    }

    private boolean startsWith(Path file, byte[] signature) throws IOException {
        byte[] header = new byte[signature.length];
        try (InputStream input = Files.newInputStream(file)) {
            if (input.readNBytes(header, 0, header.length) != header.length) return false;
        }
        return java.util.Arrays.equals(header, signature);
    }

    private boolean isZip(Path file) throws IOException {
        return startsWith(file, new byte[] { 'P', 'K', 0x03, 0x04 });
    }

    private boolean containsOfficeMarker(Path file, String requiredPrefix) throws IOException {
        boolean hasContentTypes = false;
        boolean hasRequiredPrefix = requiredPrefix == null;
        int entries = 0;
        try (ZipFile zip = new ZipFile(file.toFile())) {
            Enumeration<? extends ZipEntry> zipEntries = zip.entries();
            while (zipEntries.hasMoreElements()) {
                ZipEntry entry = zipEntries.nextElement();
                if (++entries > 10_000) throw new IllegalArgumentException("ZIPファイル内の項目数が多すぎます。");
                String name = entry.getName();
                if ("[Content_Types].xml".equals(name)) hasContentTypes = true;
                if (requiredPrefix != null && name.startsWith(requiredPrefix)) hasRequiredPrefix = true;
            }
        }
        return requiredPrefix == null ? hasContentTypes : hasContentTypes && hasRequiredPrefix;
    }

    private boolean isUtf8Text(Path file) throws IOException {
        byte[] bytes = Files.readAllBytes(file);
        for (byte value : bytes) if (value == 0) return false;
        try {
            StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(bytes));
            return true;
        } catch (CharacterCodingException e) {
            return false;
        }
    }

    private record FileType(String canonicalContentType, Set<String> acceptedContentTypes, Signature signature) {}

    private enum Signature {
        PDF, PNG, JPEG, GIF, TEXT, ZIP, DOCX, XLSX, PPTX
    }

    public record ValidatedFile(String originalName, String contentType, long size) {}
}
