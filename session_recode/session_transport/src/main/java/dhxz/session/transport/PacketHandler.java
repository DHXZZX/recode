package dhxz.session.transport;

import dhxz.session.core.PacketContext;
import dhxz.session.core.PacketContextSupport;
import dhxz.session.core.Session;
import dhxz.session.spi.PacketDirector;
import dhxz.session.spi.PacketProcessor;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.util.AttributeKey;
import poggyio.dataschemas.Packet;
import poggyio.lang.Requires;

import java.util.List;

public class PacketHandler extends ChannelDuplexHandler {
    private final PacketDirector packetDirector;
    private final List<PacketProcessor> packetProcessors;

    private volatile PacketContext packetContext;

    public PacketHandler(PacketDirector packetDirector, List<PacketProcessor> packetProcessors) {
        Requires.notNull(packetDirector, "packetDirector must not be null.");
        Requires.notNull(packetProcessors, "packetProcessors must not be null.");
        this.packetDirector = packetDirector;
        this.packetProcessors = packetProcessors;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof Packet) {
            messageReceived(ctx,(Packet) msg);
        }else {
            ctx.fireChannelRead(msg);
        }
    }

    private void messageReceived(ChannelHandlerContext ctx, Packet msg) {
        PacketContext context = packetContext;
        Packet passedPacket = packetProcessors.stream()
                .reduce(msg, (pack, proc) -> proc.upstream(context, pack), (prev, next) -> next);
        this.packetDirector.arrange(context,passedPacket);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (!(msg instanceof Packet)) {
            super.write(ctx, msg, promise);
            return;
        }

        Packet packet = (Packet) msg;
        PacketContext context = this.packetContext;

        Packet passedPacket = packetProcessors.stream()
                .reduce(packet, (pack, proc) -> proc.downstream(context, pack), (prve, next) -> next);
        super.write(ctx,passedPacket,promise);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        Session session = SessionHolder.getSession(ctx);
        PacketContext context = new PacketContextImpl(session,ctx);
    }

    private class PacketContextImpl extends PacketContextSupport {

        final ChannelHandlerContext ctx;

        public PacketContextImpl(Session session, ChannelHandlerContext ctx) {
            super(session);
            this.ctx = ctx;
        }

        @Override
        public void through(Packet proto) {
            ctx.fireChannelRead(proto);
        }

        @Override
        public void reply(Packet proto) {
            ctx.writeAndFlush(proto);
        }

        @Override
        public void close(Packet proto) {
            Object msg = null == proto ? new byte[0] : proto;
            ctx.writeAndFlush(msg).addListener(ChannelFutureListener.CLOSE);
        }

        @Override
        public void triggerEvent(Object event) {
            ctx.fireUserEventTriggered(event);
        }

        @Override
        public <T> T attr(String name) {
            AttributeKey<T> key = AttributeKey.valueOf(name);
            return ctx.attr(key).get();
        }

        @Override
        public <T> void attr(String name, T value) {
            AttributeKey<T> key = AttributeKey.valueOf(name);
            ctx.attr(key).set(value);
        }
    }
}
