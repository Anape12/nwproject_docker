package jp.nw.controller;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import jp.nw.entity.UserEntity;
import jp.nw.model.AttachmentAccessPolicy;
import jp.nw.model.AttachmentFileValidator;
import jp.nw.model.AttachmentFileValidator.ValidatedFile;
import jp.nw.model.AttachmentOwnerType;
import jp.nw.model.AttachmentScannerFactory;
import jp.nw.model.AttachmentVirusScanner;
import jp.nw.parts.DBBase;

@WebServlet("/Attachment")
@MultipartConfig(maxFileSize = AttachmentController.MAX_FILE_SIZE,
        maxRequestSize = AttachmentController.MAX_REQUEST_SIZE)
public class AttachmentController extends HttpServlet {
    static final long MAX_FILE_SIZE = 10_485_760L;
    static final long MAX_REQUEST_SIZE = 11_534_336L;

    private final AttachmentAccessPolicy accessPolicy = new AttachmentAccessPolicy();
    private final AttachmentFileValidator validator = new AttachmentFileValidator(MAX_FILE_SIZE);
    private final AttachmentVirusScanner virusScanner = AttachmentScannerFactory.create();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        request.setCharacterEncoding(StandardCharsets.UTF_8.name());
        HttpSession session = request.getSession(false);
        UserEntity user = authenticatedUser(session, response);
        if (user == null) return;

        if (!Objects.equals(session.getAttribute("attachmentCsrfToken"), request.getParameter("csrfToken"))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "不正なリクエストです。");
            return;
        }

        AttachmentOwnerType ownerType;
        String ownerId;
        try {
            ownerType = AttachmentOwnerType.parse(request.getParameter("ownerType"));
            ownerId = ownerType.validateOwnerId(request.getParameter("ownerId"));
        } catch (IllegalArgumentException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            return;
        }

        if (!accessPolicy.canWrite(user, ownerType, ownerId)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "この対象へファイルを添付する権限がありません。");
            return;
        }

        Part part;
        try {
            part = request.getPart("file");
        } catch (IllegalStateException e) {
            response.sendError(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE,
                    "ファイルサイズは10MB以下にしてください。");
            return;
        }
        if (part == null || part.getSize() <= 0) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ファイルを選択してください。");
            return;
        }

        Path storageRoot = storageRoot();
        Path temporary = Files.createTempFile(storageRoot, ".upload-", ".tmp");
        Path target = null;
        try {
            try (InputStream input = part.getInputStream()) {
                Files.copy(input, temporary, StandardCopyOption.REPLACE_EXISTING);
            }

            ValidatedFile file = validator.validate(
                    part.getSubmittedFileName(), part.getContentType(), part.getSize(), temporary);
            AttachmentVirusScanner.ScanResult scanResult = virusScanner.scan(temporary, file.originalName());
            if (!scanResult.clean()) {
                response.sendError(422,
                        scanResult.message() == null ? "安全性を確認できないファイルです。" : scanResult.message());
                return;
            }

            String storedName = UUID.randomUUID().toString();
            target = resolveStoredFile(storageRoot, storedName);
            moveIntoStorage(temporary, target);
            insertMetadata(ownerType, ownerId, user.getUserId(), file, storedName);
            response.sendRedirect(redirectPath(request, ownerType, ownerId));
        } catch (IllegalArgumentException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (SQLException e) {
            if (target != null) Files.deleteIfExists(target);
            throw new ServletException("添付ファイル情報の保存に失敗しました。", e);
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        UserEntity user = authenticatedUser(request.getSession(false), response);
        if (user == null) return;

        long attachmentId;
        try {
            attachmentId = Long.parseLong(request.getParameter("id"));
            if (attachmentId <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "添付ファイルIDが不正です。");
            return;
        }

        DBBase db = new DBBase();
        String sql = "SELECT owner_type,owner_id,original_name,stored_name,content_type,file_size "
                + "FROM attachment WHERE attachment_id=?";
        try (Connection connection = db.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, attachmentId);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }

                AttachmentOwnerType ownerType;
                try {
                    ownerType = AttachmentOwnerType.parse(result.getString("owner_type"));
                } catch (IllegalArgumentException e) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
                String ownerId = result.getString("owner_id");
                if (!accessPolicy.canRead(user, ownerType, ownerId)) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }

                Path file = resolveStoredFile(storageRoot(), result.getString("stored_name"));
                long expectedSize = result.getLong("file_size");
                if (!Files.isRegularFile(file) || Files.isSymbolicLink(file) || Files.size(file) != expectedSize) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }

                response.setContentType(result.getString("content_type"));
                response.setContentLengthLong(expectedSize);
                response.setHeader("X-Content-Type-Options", "nosniff");
                response.setHeader("Cache-Control", "private, no-store, max-age=0");
                response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''"
                        + URLEncoder.encode(result.getString("original_name"), StandardCharsets.UTF_8)
                                .replace("+", "%20"));
                Files.copy(file, response.getOutputStream());
            }
        } catch (SQLException e) {
            throw new IOException("添付ファイルの取得に失敗しました。", e);
        }
    }

    private void insertMetadata(AttachmentOwnerType ownerType, String ownerId, String userId,
            ValidatedFile file, String storedName) throws SQLException {
        String sql = "INSERT INTO attachment(owner_type,owner_id,uploaded_by_id,original_name,stored_name,content_type,file_size) "
                + "VALUES(?,?,?,?,?,?,?)";
        DBBase db = new DBBase();
        try (Connection connection = db.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, ownerType.name());
            statement.setString(2, ownerId);
            statement.setString(3, userId);
            statement.setString(4, file.originalName());
            statement.setString(5, storedName);
            statement.setString(6, file.contentType());
            statement.setLong(7, file.size());
            statement.executeUpdate();
        }
    }

    private UserEntity authenticatedUser(HttpSession session, HttpServletResponse response) throws IOException {
        if (session == null || !(session.getAttribute("loginUser") instanceof UserEntity user)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return null;
        }
        return user;
    }

    private Path storageRoot() throws IOException {
        String configured = System.getenv("ATTACHMENT_DIR");
        Path root = configured == null || configured.isBlank()
                ? Path.of(System.getProperty("java.io.tmpdir"), "nwproject-attachments")
                : Path.of(configured);
        Files.createDirectories(root);
        Path realRoot = root.toRealPath();
        if (!Files.isDirectory(realRoot)) throw new IOException("添付ファイル保存先がディレクトリではありません。");
        return realRoot;
    }

    private Path resolveStoredFile(Path root, String storedName) throws IOException {
        if (storedName == null || !storedName.matches("[0-9a-fA-F-]{36}")) {
            throw new IOException("保存ファイル名が不正です。");
        }
        Path file = root.resolve(storedName).normalize();
        if (!file.getParent().equals(root)) throw new IOException("保存先が不正です。");
        return file;
    }

    private void moveIntoStorage(Path source, Path target) throws IOException {
        try {
            Files.move(source, target, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(source, target);
        }
    }

    private String redirectPath(HttpServletRequest request, AttachmentOwnerType type, String ownerId) {
        String context = request.getContextPath();
        String encodedId = URLEncoder.encode(ownerId, StandardCharsets.UTF_8);
        return switch (type) {
            case REPORT -> context + "/DairyWrite?edit=" + encodedId;
            case CHAT -> context + "/ChatChanelRoom?roomId=" + encodedId;
            case SCHEDULE -> context + "/OpenCalender?edit=" + encodedId;
            case APPROVAL -> context + "/ReportApproval?id=" + encodedId;
        };
    }
}
