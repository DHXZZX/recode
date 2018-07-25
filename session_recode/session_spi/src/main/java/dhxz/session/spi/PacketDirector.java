package dhxz.session.spi;

import dhxz.session.core.PacketContext;
import poggyio.dataschemas.Packet;
import poggyio.dataschemas.PacketException;
import poggyio.logging.Loggers;

public interface PacketDirector {
    void action(PacketContext context);

    default void arrange(PacketContext context, Packet packet) {
        try {
            context.advance(packet);
        }catch (Exception e) {
            Loggers.me().error(getClass(),"director arrange error.",e);
            throw new PacketException("packet arrange error.",e);
        }
    }
}
