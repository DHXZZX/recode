package dhxz.session.transport;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import poggyio.dataschemas.Message;
import poggyio.dataschemas.Packet;
import poggyio.logging.Loggers;

import java.util.List;

public class MessageEncoder extends MessageToMessageEncoder<Message> {

    final ObjectMapper mapper;

    MessageEncoder(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, List<Object> out) throws Exception {
        byte[] body = mapper.writeValueAsBytes(msg);
        Packet packet = Packet.empty().type(Packet.POST_TYPE).body(body);
        out.add(packet);
        Loggers.me().info(
                getClass(),
                "encode message success. from:{}, to:{}, bodyType:{}, digestSize:{}, bodySize:{}.",
                msg.from(), msg.to(), msg.bodyType(), packet.digestSize(), packet.bodySize()
        );
    }
}
