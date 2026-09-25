package jp.nw.model;

import java.io.IOException;
import java.nio.file.Path;

/**
 * ウイルススキャン製品との接続点。実装をServiceLoaderへ登録すると自動的に使用される。
 */
public interface AttachmentVirusScanner {
    ScanResult scan(Path file, String originalName) throws IOException;

    record ScanResult(boolean clean, String message) {
        public static ScanResult accepted() {
            return new ScanResult(true, null);
        }

        public static ScanResult rejected(String message) {
            return new ScanResult(false, message);
        }
    }
}
