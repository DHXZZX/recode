package dhxz.session.transport;

public class SessionTransportException extends RuntimeException {
    private static final long serialVersionUID = 623060766606852704L;

    public SessionTransportException() {
    }

    public SessionTransportException(String message) {
        super(message);
    }

    public SessionTransportException(String message, Throwable cause) {
        super(message, cause);
    }

    public SessionTransportException(Throwable cause) {
        super(cause);
    }
}
