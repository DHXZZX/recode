package dhxz.session.api;

public interface SessionService {

    void post(SessionPostRequest request);

    void close(SessionCloseRequest request);
}
