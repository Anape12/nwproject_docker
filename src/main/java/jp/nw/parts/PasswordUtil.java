package jp.nw.parts;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class PasswordUtil {
    private static final PasswordEncoder ENCODER = new BCryptPasswordEncoder();

    private PasswordUtil() {
        // インスタンス生成禁止
    }

    /**
     * パスワードをハッシュ化
     */
    public static String encode(String password) {
        return ENCODER.encode(password);
    }

    /**
     * パスワード照合
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        return ENCODER.matches(rawPassword, encodedPassword);
    }
}
