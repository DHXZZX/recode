package dhxz.session.api;

public class SessionException extends RuntimeException{
    private static final long serialVersionUID = -2606728501892650962L;
    public SessionException() {
    }

    public SessionException(String message) {
        super(message);
    }

    public SessionException(String message, Throwable cause) {
        super(message, cause);
    }

    public SessionException(Throwable cause) {
        super(cause);
    }
}
