package dhxz.session.core;

import poggyio.commons.logger.Loggers;

import java.net.URI;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author 10066610
 * session 容器
 */
public class SessionRegistry {
    private final ConcurrentMap<String,Session> sessions = new ConcurrentHashMap<>();

    public Optional<Session> lookup(String remoteEndpoint) {
        return lookup(URI.create(remoteEndpoint));
    }

    private Optional<Session> lookup(URI remoteEndpoint) {
        Session session = this.sessions.get(calcSessionKey(remoteEndpoint));
        return Optional.ofNullable(session);
    }

    private String calcSessionKey(URI remoteEndpoint) {
        return String.format("%s-%s-%s",remoteEndpoint.getScheme(),remoteEndpoint.getHost(),remoteEndpoint.getPort());
    }
    public void register(Session session) {
        URI uri = URI.create(session.metadata().remoteEndpoint());
        Loggers.me().debug(getClass(),"session[{}] registered.",uri);
        this.sessions.put(calcSessionKey(uri),session);
    }

    public void unregister(Session session) {
        URI uri = URI.create(session.metadata().remoteEndpoint());
        Loggers.me().debug(getClass(),"session[{}] registered. ",uri);
        this.sessions.remove(calcSessionKey(uri));
    }
}
