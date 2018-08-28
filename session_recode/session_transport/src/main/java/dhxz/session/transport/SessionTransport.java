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
            throw new SessionTransportException("start session transport occurs error.", e);
        }
    }

    protected abstract ChannelInitializer<Channel> doGetChannelInitializer(SslContext sslContext);

    @Override
    protected void doStop() {
        if(null != serviceBossGroup) {
            try {
                serviceBossGroup.shutdownGracefully().sync();
            } catch (InterruptedException ignore) {

            }
        }

        if (null != serviceWorkGroup) {
            try {
                serviceWorkGroup.shutdownGracefully().sync();
            } catch (InterruptedException ignore) {

            }
        }
    }

    public final SessionRegistry sessionRegistry(){
        return sessionRegistry;
    }

    public final SessionTransportConfig sessionConfig() {
        return sessionConfig;
    }

    public final PacketDirector protocolDirector() {
        return packetDirector;
    }
    public final List<PacketProcessor> protocolProcessors() {
        return packetProcessors;
    }
    public final void setPacketProcessors(List<PacketProcessor> packetProcessors) {
        Assert.notNull(packetProcessors, "packetProcessors must not be null.");
        this.packetProcessors = packetProcessors;
    }

    /**
     * @return
     */
    public final List<MessageHandler> messageHandlers() {
        return messageHandlers;
    }

    /**
     * @param messageHandlers
     */
    public final void setMessageHandlers(List<MessageHandler> messageHandlers) {
        Assert.notNull(messageHandlers, "messageHandlers must not be null.");
        this.messageHandlers = messageHandlers;
    }

    /**
     * @return
     */
    public final List<MessageInterceptor> messageInterceptors() {
        return messageInterceptors;
    }

    /**
     * @param messageInterceptors
     */
    public final void setMessageInterceptors(List<MessageInterceptor> messageInterceptors) {
        Assert.notNull(messageInterceptors, "messageInterceptors must not be null.");
        this.messageInterceptors = messageInterceptors;
    }

    /**
     * @return
     */
    public final SessionListener sessionListener() {
        return sessionListener;
    }

    /**
     * @param sessionListener
     */
    public final void setSessionListener(SessionListener sessionListener) {
        Assert.notNull(sessionListener, "sessionListener must not be null.");
        this.sessionListener = sessionListener;
    }
    protected int getIdleTimeout() {
        return sessionConfig().intParamOrDefault("idle.timeout", getIdleCheckPeriod() * 3);
    }

    protected int getIdleInitTimeout() {
        return sessionConfig().intParamOrDefault("init.idle.timeout", getIdleCheckPeriod());
    }
    protected int getIdleCheckPeriod() {
        return sessionConfig().intParamOrDefault("idle.period", 180);
    }

    protected long getTrafficLimit() {
        return sessionConfig().longParamOrDefault("traffic.limit", 16384L);
    }

    protected long getTrafficCheckInterval() {
        return sessionConfig().longParamOrDefault("traffic.interval", 16000L);
    }

    protected boolean isSslEnabled() {
        return sessionConfig().booleanParamOrDefault("ssl.enabled", false);
    }

    protected boolean isSslVerify() {
        return sessionConfig().booleanParamOrDefault("ssl.verify", false);
    }

    protected String getSslCrt() {
        return sessionConfig().paramOrDefault("ssl.crt", "");
    }

    protected String getSslKey() {
        return sessionConfig().paramOrDefault("ssl.key", "");
    }

    protected SslContext getSslContext() throws SSLException {
        Loggers.me().warn(getClass(), "load ssl[key={}, crt={}].", getSslKey(), getSslCrt());
        return SslContextBuilder.forServer(
                getClass().getResourceAsStream(getSslCrt()),
                getClass().getResourceAsStream(getSslKey())
        ).clientAuth(ClientAuth.NONE).build();
    }
}
