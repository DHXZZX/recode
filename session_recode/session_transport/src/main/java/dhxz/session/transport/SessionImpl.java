package dhxz.session.transport;

import dhxz.session.core.SessionSupport;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import poggyio.dataschemas.IData;
import poggyio.dataschemas.Message;

import java.util.Optional;

public class SessionImpl extends SessionSupport {
    final ChannelHandlerContext ctx;
    volatile Message lastHandledMessage;
    public SessionImpl(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public Optional<Message> lastHandledMessage() {
        return Optional.ofNullable(lastHandledMessage);
    }

    @Override
    public void reply(IData data) {
        if (null == lastHandledMessage && data instanceof Message) {
            lastHandledMessage = (Message) data;
        }
        if (null != lastHandledMessage && data instanceof Message) {
            Message out = (Message) data;
            if (lastHandledMessage.deliveryTime() < out.deliveryTime()) {
                lastHandledMessage = out;
            }
        }
        this.ctx.writeAndFlush(data);
    }

    @Override
    public void close(IData data) {
        Object out = null == data ? new byte[0] : data;
        this.ctx.writeAndFlush(out).addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public String toString() {
        return "SessionImpl{" +
                "ctx=" + ctx +
                '}';
    }
}
