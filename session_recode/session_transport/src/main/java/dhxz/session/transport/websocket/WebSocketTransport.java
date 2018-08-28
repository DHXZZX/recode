package dhxz.session.transport.websocket;

import dhxz.session.core.SessionRegistry;
import dhxz.session.spi.PacketDirector;
import dhxz.session.transport.*;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketFrameAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.ssl.SslContext;

public class WebSocketTransport extends SessionTransport{
    public WebSocketTransport(SessionRegistry sessionRegistry, SessionTransportConfig sessionConfig, PacketDirector packetDirector) {
        super(sessionRegistry, sessionConfig, packetDirector);
    }

    @Override
    protected ChannelInitializer<Channel> doGetChannelInitializer(SslContext sslContext) {
        final int idleTimeout = getIdleTimeout();
        final int idleInitTimeout = getIdleInitTimeout();
        final int idleCheckPeriod = getIdleCheckPeriod();

        return new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel ch) throws Exception {
                if (null != sslContext) {
                    ch.pipeline().addLast(sslContext.newHandler(ch.alloc()));
                }
                ch.pipeline().addLast(new SessionTrafficHandler(getTrafficLimit(),getTrafficCheckInterval()));
                ch.pipeline().addLast(new HttpServerCodec());
                ch.pipeline().addLast(new HttpObjectAggregator(65535));
                ch.pipeline().addLast(new WebSocketServerProtocolHandler("/"));
                ch.pipeline().addLast(new WebSocketFrameAggregator(64));
                ch.pipeline().addLast(new WebSocketCodec());
                ch.pipeline().addLast(new PacketCodec());
                ch.pipeline().addLast(new SessionIdleHandler(idleCheckPeriod));
                ch.pipeline().addLast(new PacketHandler(protocolDirector(), protocolProcessors()));
                ch.pipeline().addLast(new MessageCodec());
                ch.pipeline().addLast(new MessageUnifiedCodec());
                ch.pipeline().addLast(new MessageHandler(messageHandlers(), messageInterceptors()));
                ch.pipeline().addLast(new SessionIdleStateHandler(idleTimeout, idleInitTimeout));
                ch.pipeline().addLast(new SessionHandler(sessionRegistry(), sessionConfig(), sessionListener()));
            }
        };
    }
}
