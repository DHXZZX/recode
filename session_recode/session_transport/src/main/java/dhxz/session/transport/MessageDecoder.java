package dhxz.session.transport;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import poggyio.dataschemas.Message;
import poggyio.dataschemas.Packet;
import poggyio.logging.Loggers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageDecoder extends MessageToMessageDecoder<Packet> {
    final ObjectMapper mapper;

    public MessageDecoder(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, Packet msg, List<Object> out) throws Exception {
        // convert body as Message directly will lost Message.body type info
        TypeFactory typeFactory = mapper.getTypeFactory();
        MapType type = typeFactory.constructMapType(HashMap.class, String.class, Object.class);
        Map<String,Object> content =  mapper.readValue(msg.body(),type);
        Message message = mapper.convertValue(content,Message.class);
        out.add(message);

        Loggers.me().info(
                getClass(),
                "decode message success, from:{}, to:{}, bodyType:{}, digestSize:{}, bodySize:{}",
                message.from(),message.to(),message.bodyType(),msg.digestSize(),msg.bodySize()
        );
    }
}
