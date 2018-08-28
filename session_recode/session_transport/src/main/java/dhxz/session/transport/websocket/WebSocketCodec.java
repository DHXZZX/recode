package dhxz.session.transport.websocket;

import io.netty.channel.CombinedChannelDuplexHandler;

public class WebSocketCodec extends CombinedChannelDuplexHandler<WebSocketFrameDecoder,WebSocketFrameEncoder> {
    public WebSocketCodec() {
        super(new WebSocketFrameDecoder(),new WebSocketFrameEncoder());
    }
}
