package dhxz.session.discovery;

public class DiscoveryException extends RuntimeException{
    private static final long serialVersionUID = 5174709978489839502L;

    public DiscoveryException() {
    }

    public DiscoveryException(String message) {
        super(message);
    }

    public DiscoveryException(String message, Throwable cause) {
        super(message, cause);
    }

    public DiscoveryException(Throwable cause) {
        super(cause);
    }
}
