package dhxz.session.core;


import poggyio.dataschemas.Packet;

public interface PacketContext {

    interface State {
        void advance(PacketContext context, Packet packet) throws Exception;
    }

    void advance(Packet packet) throws Exception;

    void through(Packet proto);

    void reply(Packet proto);

    void close(Packet proto);

    void triggerEvent(Object event);

    PacketContext.State state();

    void state(PacketContext.State state);

    <T> T attr(String name);

    <T> void attr(String name, T value);

    Session session();
}
