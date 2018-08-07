package dhxz.session.transport;

import io.netty.handler.timeout.IdleStateHandler;

public class SessionIdleHandler extends IdleStateHandler {
    public SessionIdleHandler(int idleSeconds) {
        super(0, 0, idleSeconds);
    }
}
