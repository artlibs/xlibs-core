package cn.xlibs.lib4j.core.exception;

/**
 * OperationException
 *
 * @author Fury
 * @since 2021-04-17
 * <p>
 * All rights Reserved.
 */
public final class OperationException extends RuntimeException {
    private OperationException() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    public OperationException(String message) {
        super(message);
    }

    /**
     * {@inheritDoc}
     */
    public OperationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * {@inheritDoc}
     */
    public OperationException(Throwable cause) {
        super(cause);
    }

    /**
     * {@inheritDoc}
     */
    protected OperationException(String message, Throwable cause,
                               boolean enableSuppression,
                               boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
