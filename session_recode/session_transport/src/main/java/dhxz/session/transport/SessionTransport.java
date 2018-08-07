package dhxz.session.transport;

import dhxz.session.core.SessionRegistry;
import dhxz.session.spi.MessageHandler;
import dhxz.session.spi.MessageInterceptor;
import dhxz.session.spi.PacketDirector;
import dhxz.session.spi.PacketProcessor;
import dhxz.session.spi.event.SessionListener;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import org.dayatang.utils.Assert;
import poggyio.component.AbstractComponent;
import poggyio.logging.Loggers;

import javax.net.ssl.SSLException;
import java.util.Collections;
import java.util.List;

public abstract class SessionTransport extends AbstractComponent {

    private static final String IO_RATIO = "io.ratio";
    private static final String TCP_BACKLOG = "tcp.backlog";
    private static final String TCP_RECYCLE = "tcp.recycle";

    private static final float DEFAULT_IO_RATIO = 0.9F;
    private static final int DEFAULT_TCP_BACKLOG = 8192;
    private static final boolean DEFAULT_TCP_RECYCLE = false;

    private EventLoopGroup serviceBossGroup;
    private EventLoopGroup serviceWorkGroup;

    private final SessionRegistry sessionRegistry;
    private final SessionTransportConfig sessionConfig;
    private final PacketDirector packetDirector;

    private List<PacketProcessor> packetProcessors = Collections.emptyList();
    private List<MessageHandler> messageHandlers = Collections.emptyList();
    private List<MessageInterceptor> messageInterceptors = Collections.emptyList();
    private SessionListener sessionListener = evt->{};

    public SessionTransport(SessionRegistry sessionRegistry, SessionTransportConfig sessionConfig, PacketDirector packetDirector) {
        Assert.notNull(sessionRegistry, "sessionRegistry must not be null.");
        Assert.notNull(sessionConfig, "sessionConfig must not be null.");
        Assert.notNull(packetDirector, "packetDirector must not be null.");
        this.sessionRegistry = sessionRegistry;
        this.sessionConfig = sessionConfig;
        this.packetDirector = packetDirector;
    }

    @Override
    protected void doStart() {
        try {
            int processors = Runtime.getRuntime().availableProcessors();
            serviceBossGroup = new NioEventLoopGroup(processors);
            float ioRatio = sessionConfig.floatParamOrDefault(IO_RATIO, DEFAULT_IO_RATIO);
            serviceWorkGroup = new NioEventLoopGroup((int)(processors / (1 - ioRatio)));

            String host = sessionConfig.host();
            int port = sessionConfig.port();
            int tcpBacklog = sessionConfig.intParamOrDefault(TCP_BACKLOG, DEFAULT_TCP_BACKLOG);
            boolean tcpRecycle = sessionConfig.booleanParamOrDefault(TCP_RECYCLE, DEFAULT_TCP_RECYCLE);
            ChannelInitializer<Channel> channelInitializer = doGetChannelInitializer(isSslEnabled()?getSslContext():null);

            new ServerBootstrap()
                    .group(serviceBossGroup,serviceWorkGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.DEBUG))
                    .childHandler(channelInitializer)
                    .option(ChannelOption.SO_REUSEADDR,tcpRecycle)
                    .option(ChannelOption.SO_BACKLOG,tcpBacklog)
                    .bind(host,port)
                    .sync();
        } catch (SSLException e) {
            throw new SessionTransportException("start session transport occurs error.", e);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    protected abstract ChannelInitializer<Channel> doGetChannelInitializer(SslContext sslContext);

    private SslContext getSslContext() throws SSLException {
        Loggers.me().warn(getClass(),"load ssl[key={}, crt={}]",getSslKey(),getSslCrt());
        return SslContextBuilder.forServer(
                getClass().getResourceAsStream(getSslCrt()),
                getClass().getResourceAsStream(getSslKey())
        ).clientAuth(ClientAuth.NONE).build();
    }

    private String getSslKey() {
        return sessionConfig.paramOrDefault("ssl.key","");
    }

    private String getSslCrt() {
        return sessionConfig.paramOrDefault("ssl.crt","");
    }

    private boolean isSslEnabled() {
        return sessionConfig.booleanParamOrDefault("ssl.enabled",false);
    }

    @Override
    protected void doStop() {

    }
}
