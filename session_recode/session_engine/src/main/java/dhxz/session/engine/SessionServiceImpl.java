package dhxz.session.engine;

import dhxz.session.api.SessionCloseRequest;
import dhxz.session.api.SessionNotFoundException;
import dhxz.session.api.SessionPostRequest;
import dhxz.session.api.SessionService;
import dhxz.session.core.Session;
import dhxz.session.core.SessionRegistry;
import poggyio.lang.Requires;
import poggyio.logging.Loggers;

public class SessionServiceImpl implements SessionService {

    private final SessionRegistry registry;

    public SessionServiceImpl(SessionRegistry registry) {
        Requires.notNull(registry,"registry must not be null");
        this.registry = registry;
    }

    private Session getSession(String remoteEndpoint) {
        return registry.lookup(remoteEndpoint).orElseThrow(SessionNotFoundException::new);
    }

    @Override
    public void post(SessionPostRequest request) {
        Loggers.me().info(getClass(),"post data {}.",request.data());
        getSession(request.remoteEndpoint()).reply(request.data());
    }

    @Override
    public void close(SessionCloseRequest request) {
        getSession(request.remoteEndpoint()).close(request.data());
    }
}
