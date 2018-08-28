package dhxz.session.transport;

import io.netty.handler.traffic.ChannelTrafficShapingHandler;

public class SessionTrafficHandler extends ChannelTrafficShapingHandler {
    public SessionTrafficHandler(long readLimit, long checkInterval) {
        super(0, readLimit, checkInterval);
    }
}
