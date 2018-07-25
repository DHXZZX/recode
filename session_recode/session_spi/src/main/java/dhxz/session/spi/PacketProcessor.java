package dhxz.session.spi;

import dhxz.session.core.PacketContext;
import poggyio.dataschemas.Packet;

public interface PacketProcessor {
    Packet unstream(PacketContext context, Packet packet);

    Packet downstream(PacketContext context, Packet packet);
}
