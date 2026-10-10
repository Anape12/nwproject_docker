package jp.nw.model;

/** Application errors carry a stable code; display text is resolved separately. */
public final class CodedException {
    private CodedException() {
    }

    public interface Coded {
        String errorCode();
    }

    public static final class Validation extends IllegalArgumentException implements Coded {
        private final String errorCode;

        public Validation(String errorCode) {
            super(errorCode);
            this.errorCode = errorCode;
        }

        @Override
        public String errorCode() {
            return errorCode;
        }
    }

    public static final class Denied extends SecurityException implements Coded {
        private final String errorCode;

        public Denied(String errorCode) {
            super(errorCode);
            this.errorCode = errorCode;
        }

        @Override
        public String errorCode() {
            return errorCode;
        }
    }

    public static final class Failure extends RuntimeException implements Coded {
        private final String errorCode;

        public Failure(String errorCode) {
            super(errorCode);
            this.errorCode = errorCode;
        }

        public Failure(String errorCode, Throwable cause) {
            super(errorCode, cause);
            this.errorCode = errorCode;
        }

        @Override
        public String errorCode() {
            return errorCode;
        }
    }
}
