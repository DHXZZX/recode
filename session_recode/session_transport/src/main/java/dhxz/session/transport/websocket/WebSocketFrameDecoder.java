package dhxz.session.transport.websocket;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import poggyio.logging.Loggers;

import java.util.List;

public class WebSocketFrameDecoder extends MessageToMessageDecoder<BinaryWebSocketFrame> {

    @Override
    protected void decode(ChannelHandlerContext ctx, BinaryWebSocketFrame msg, List<Object> out) throws Exception {
        Loggers.me().debug(getClass(),"read socket frame[{}],{} bytes",msg,msg.content().readableBytes());
        out.add(msg.retain().content());
    }
}
