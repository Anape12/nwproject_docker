package jp.nw.model;

import java.nio.file.Path;
import java.util.ServiceLoader;

public final class AttachmentScannerFactory {
    private AttachmentScannerFactory() {}

    public static AttachmentVirusScanner create() {
        return ServiceLoader.load(AttachmentVirusScanner.class).findFirst().orElseGet(NoOpScanner::new);
    }

    private static final class NoOpScanner implements AttachmentVirusScanner {
        @Override
        public ScanResult scan(Path file, String originalName) {
            return ScanResult.accepted();
        }
    }
}
