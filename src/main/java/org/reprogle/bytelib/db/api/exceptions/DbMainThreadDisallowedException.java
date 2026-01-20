package org.reprogle.bytelib.db.api.exceptions;

public class DbMainThreadDisallowedException extends RuntimeException {
    public DbMainThreadDisallowedException(String message) {
        super(message);
    }
}
