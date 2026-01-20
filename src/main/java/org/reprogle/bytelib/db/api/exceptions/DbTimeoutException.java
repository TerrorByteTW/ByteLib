package org.reprogle.bytelib.db.api.exceptions;

public final class DbTimeoutException extends RuntimeException {
    public DbTimeoutException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public DbTimeoutException(String msg) {
        super(msg);
    }
}
