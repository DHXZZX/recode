package dhxz.session.transport.websocket;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import poggyio.logging.Loggers;

import java.util.List;

public class WebSocketFrameEncoder extends MessageToMessageEncoder<ByteBuf> {
    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        Loggers.me().debug(getClass(), "write socket frame[{}], {} bytes.", msg, msg.readableBytes());
        out.add(new BinaryWebSocketFrame(msg).retain());
    }
}
