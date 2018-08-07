package dhxz.session.transport;

import dhxz.session.core.Session;
import dhxz.session.core.SessionRegistry;
import dhxz.session.spi.event.SessionEvent;
import dhxz.session.spi.event.SessionListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.net.InetSocketAddress;

public class SessionHandler extends ChannelInboundHandlerAdapter {
    final SessionRegistry sessionRegistry;
    final SessionTransportConfig sessionConfig;
    final SessionListener sessionListener;

    public SessionHandler(SessionRegistry sessionRegistry, SessionTransportConfig sessionConfig, SessionListener sessionListener) {
        this.sessionRegistry = sessionRegistry;
        this.sessionConfig = sessionConfig;
        this.sessionListener = sessionListener;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (!(evt instanceof SessionEvent)){
            ctx.fireUserEventTriggered(evt);
            return;
        }
        SessionEvent event = (SessionEvent) evt;
        fire(sessionListener,event);
    }

    private void fire(SessionListener sessionListener,SessionEvent event) {
        sessionListener.onEvent(event);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Session.Metadata metadata = Session.Metadata.builder()
                .state(Session.State.UNAPPROVED)
                .accessEndpoint(sessionConfig.accessEndpoint().toASCIIString())
                .remoteEndpoint(ofRemoteEndpoint(ctx))
                .build();
        Session session = new SessionImpl(ctx);
        session.metadata(metadata);
        SessionHolder.setSession(ctx,session);
        sessionRegistry.register(session);
        userEventTriggered(ctx,openedEventWith(session));
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Session session = SessionHolder.getAndRemoveSession(ctx);
        userEventTriggered(ctx,closedEventWith(session));
        sessionRegistry.unregister(session);
    }

    private SessionEvent closedEventWith(Session session) {
        return SessionEvent.of(SessionEvent.Type.CLOSED,session);
    }

    private SessionEvent openedEventWith(Session session) {
        return SessionEvent.of(SessionEvent.Type.OPENED,session);
    }

    private String ofRemoteEndpoint(ChannelHandlerContext ctx) {
        /**
         * Important. using remoteAddress.getHostName may cause a dns lookup.
         * use remoteAddress.getHostString instead.
         */
        InetSocketAddress remoteAddress = (InetSocketAddress)ctx.channel().remoteAddress();
        return String.format("%s://%s:%s",sessionConfig.scheme(),remoteAddress.getHostString(),remoteAddress.getPort());
    }


}
