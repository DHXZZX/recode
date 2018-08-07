package dhxz.session.transport;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import poggyio.dataschemas.Message;
import poggyio.lang.ObjectStyles;

import java.util.List;
import java.util.Map;

public class MessageUnifiedDecoder extends MessageToMessageDecoder<Message>{
    @Override
    @SuppressWarnings("unchecked")
    protected void decode(ChannelHandlerContext ctx, Message msg, List<Object> out) throws Exception {
        Map<String,Object> body = (Map<String,Object>) ObjectStyles.CAMEL_CASE_TO_SNAKE_CASE.unify(msg.body());
        Message outMsg = msg.toBuilder()
                .body(body)
                .build();
        out.add(outMsg);
    }
}
