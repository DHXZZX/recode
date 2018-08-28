package dhxz.session.transport;

import dhxz.session.core.Session;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
import poggyio.logging.Loggers;

public class SessionIdleStateHandler extends ChannelInboundHandlerAdapter {
    final long idleTimeout;
    final long idleInitTimeout;
    volatile long lastIdleTime = -1;

    public SessionIdleStateHandler(long idleTimeout, long idleInitTimeout) {
        this.idleTimeout = idleTimeout;
        this.idleInitTimeout = idleInitTimeout;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (!(evt instanceof IdleStateEvent)) {
            ctx.fireUserEventTriggered(evt);
            return;
        }

        IdleStateEvent idleEvt = (IdleStateEvent) evt;
        if (idleEvt == IdleStateEvent.FIRST_ALL_IDLE_STATE_EVENT) {
            lastIdleTime = System.currentTimeMillis();
            return;
        }
        if (idleEvt != IdleStateEvent.ALL_IDLE_STATE_EVENT) {
            return;
        }
        if (isIdleTimeoutNow(ctx)) {
            Loggers.me().info(getClass(), "idle timeout reached, close session.");
            ctx.close();
        }
    }

    private boolean isIdleTimeoutNow(ChannelHandlerContext ctx) {
        long now = System.currentTimeMillis();
        Session session = SessionHolder.getSession(ctx);
        return (session.metadata().state() == Session.State.APPROVED) ? now - lastIdleTime >= idleTimeout : now - lastIdleTime >= idleInitTimeout;
    }
}
