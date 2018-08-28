package dhxz.session.transport;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import poggyio.dataschemas.Message;
import poggyio.lang.ObjectStyles;

import java.util.List;
import java.util.Map;

public class MessageUnifiedEncoder extends MessageToMessageEncoder<Message>{
    @Override
    @SuppressWarnings("unchecked")
    protected void encode(ChannelHandlerContext ctx, Message msg, List<Object> out) throws Exception {
        long now = System.currentTimeMillis();
        Map<String,Object> body = (Map<String, Object>) ObjectStyles.CAMEL_CASE_TO_SNAKE_CASE.unify(msg.body());
        Message outMsg = msg.toBuilder()
                .body(body)
                .deliveryTime(now)
                .build();
        out.add(outMsg);
    }
}
