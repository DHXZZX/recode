package dhxz.session.transport;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import poggyio.dataschemas.Packet;
import poggyio.dataschemas.PacketException;
import poggyio.logging.Loggers;

public class PacketEncoder extends MessageToByteEncoder<Packet> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Packet msg, ByteBuf out) throws Exception {
        if (isUnexpectDigest(msg)) {
            throw new PacketException("digestSize " + msg.digestSize() + "over limit or invalid.");
        }
        if (isUnexpectBody(msg)) {
            throw new PacketException("bodySize " + msg.bodySize() + "over limit or invalid.");
        }

        int firstByte = (msg.type() << 4) | msg.qos();

        out.writeByte(firstByte);
        out.writeByte(msg.digestSize());
        out.writeBytes(msg.digest());
        out.writeShort(msg.bodySize());
        out.writeBytes(msg.body());
        Loggers.me().info(getClass(), "{} encode success.", msg);
    }

    private boolean isUnexpectDigest(Packet packet) {
        return packet.digestSize() > Packet.MAX_DIGEST_SIZE || packet.digestSize() < Packet.MIN_DIGEST_SIZE;
    }

    private boolean isUnexpectBody(Packet packet) {
        return packet.bodySize() > Packet.MAX_BODY_SIZE || packet.bodySize() < Packet.MIN_BODY_SIZE;
    }
}
