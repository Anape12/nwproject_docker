package jp.nw.model;

/** Application errors carry a stable code; display text is resolved separately. */
public final class CodedException {
    private CodedException() {
    }

    public interface Coded {
        ErrorCode errorCode();
    }

    public static final class Validation extends IllegalArgumentException implements Coded {
        private final ErrorCode errorCode;

        public Validation(ErrorCode errorCode) {
            super(errorCode.code());
            this.errorCode = errorCode;
        }

        @Override
        public ErrorCode errorCode() {
            return errorCode;
        }
    }

    public static final class Denied extends SecurityException implements Coded {
        private final ErrorCode errorCode;

        public Denied(ErrorCode errorCode) {
            super(errorCode.code());
            this.errorCode = errorCode;
        }

        @Override
        public ErrorCode errorCode() {
            return errorCode;
        }
    }

    public static final class Failure extends RuntimeException implements Coded {
        private final ErrorCode errorCode;

        public Failure(ErrorCode errorCode) {
            super(errorCode.code());
            this.errorCode = errorCode;
        }

        public Failure(ErrorCode errorCode, Throwable cause) {
            super(errorCode.code(), cause);
            this.errorCode = errorCode;
        }

        @Override
        public ErrorCode errorCode() {
            return errorCode;
        }
    }
}
