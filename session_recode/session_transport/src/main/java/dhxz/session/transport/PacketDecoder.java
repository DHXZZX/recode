package dhxz.session.transport;


import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import io.netty.util.AttributeKey;
import poggyio.dataschemas.Packet;
import poggyio.dataschemas.PacketException;
import poggyio.logging.Loggers;

import java.util.List;

public class PacketDecoder extends ReplayingDecoder<PacketDecoder.State> {

    static final AttributeKey<Packet> PROTOCOL_KEY = AttributeKey.valueOf("PROTOCOL_KEY");

    PacketDecoder() {
        super(State.INIT);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        switch (state()) {
            case INIT:
                initProtocol(ctx);
                checkpoint(State.FIRST_BYTE);
            case FIRST_BYTE:
                decodeFirstByte(ctx, in);
                checkpoint(State.DIGEST_SIZE);
            case DIGEST_SIZE:
                decodeDigestSize(ctx, in);
                checkpoint(State.DIGEST);
            case DIGEST:
                decodeDigest(ctx, in);
                checkpoint(State.BODY_SIZE);
            case BODY_SIZE:
                decodeBodySize(ctx, in);
                checkpoint(State.BODY);
            case BODY:
                decodeBody(ctx, in, out);
                checkpoint(State.INIT);
        }
    }

    private void decodeBody(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        Packet packet = ctx.channel().attr(PROTOCOL_KEY).get();
        Loggers.me().info(getClass(), "decode body, expect {} bytes.", packet.bodySize());
        byte[] body = new byte[packet.bodySize()];
        in.readBytes(body, 0, body.length);
        packet = packet.body(body);
        ctx.channel().attr(PROTOCOL_KEY).set(packet);
        out.add(packet);
        Loggers.me().info(getClass(), "{} decode success.", packet);
    }

    private void decodeBodySize(ChannelHandlerContext ctx, ByteBuf in) {
        Packet packet = ctx.channel().attr(PROTOCOL_KEY).get();
        Loggers.me().info(getClass(), "decode body size.");
        int bodySize = in.readUnsignedShort();
        if ((isUnexpectBody(bodySize))) {
            throw new PacketException("bodySize " + bodySize + "over limit or invalid.");
        }
        packet = packet.bodySize(bodySize);
        ctx.channel().attr(PROTOCOL_KEY).set(packet);
    }

    private boolean isUnexpectBody(int bodySize) {
        return bodySize > Packet.MAX_BODY_SIZE || bodySize < Packet.MIN_BODY_SIZE;
    }

    private void decodeDigest(ChannelHandlerContext ctx, ByteBuf in) {
        Packet packet = ctx.channel().attr(PROTOCOL_KEY).get();
        byte[] digest = new byte[packet.digestSize()];
        in.readBytes(digest, 0, digest.length);
        Loggers.me().info(getClass(), "decode digest,expect {} bytes", packet.digestSize());
        packet = packet.digest(digest);
        ctx.channel().attr(PROTOCOL_KEY).set(packet);
    }

    private void decodeDigestSize(ChannelHandlerContext ctx, ByteBuf in) {
        Packet packet = ctx.channel().attr(PROTOCOL_KEY).get();
        short digestSize = in.readUnsignedByte();
        Loggers.me().info(getClass(), "decode digest size");
        if (isUnexpectDigest(digestSize)) {
            throw new PacketException("digestSize" + digestSize + "over limit or invalid.");
        }
        packet = packet.digestSize(digestSize);
        ctx.channel().attr(PROTOCOL_KEY).set(packet);
    }

    private boolean isUnexpectDigest(short digestSize) {
        return digestSize > Packet.MAX_DIGEST_SIZE || digestSize < Packet.MIN_DIGEST_SIZE;
    }

    private void decodeFirstByte(ChannelHandlerContext ctx, ByteBuf in) {
        short firstByte = in.readUnsignedByte();
        Loggers.me().info(getClass(), "decode first byte.");
        int type = (firstByte >> 4) & 0x0F;
        int qos = firstByte & 0x0F;
        Packet packet = ctx.channel().attr(PROTOCOL_KEY).get().type(type).qos(qos);
        ctx.channel().attr(PROTOCOL_KEY).set(packet);
    }

    private void initProtocol(ChannelHandlerContext ctx) {
        ctx.attr(PROTOCOL_KEY).set(Packet.empty());
    }

    enum State {
        INIT,
        FIRST_BYTE,
        DIGEST_SIZE,
        DIGEST,
        BODY_SIZE,
        BODY
    }
}
