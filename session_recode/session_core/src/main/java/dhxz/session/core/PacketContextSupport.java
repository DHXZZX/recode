package dhxz.session.core;

import poggyio.dataschemas.Packet;
import poggyio.lang.Requires;

import java.util.concurrent.atomic.AtomicReference;

public abstract class PacketContextSupport implements PacketContext {
    private final AtomicReference<State> stateRef = new AtomicReference<>();
    private final Session session;

    public PacketContextSupport(Session session) {
        Requires.notNull(session,"session must not be null");
        this.session = session;
    }

    @Override
    public final void advance(Packet packet) throws Exception {
        State state = state();
        state.advance(this,packet);
    }

    @Override
    public final State state() {
        return this.stateRef.get();
    }

    @Override
    public final void state(State state) {
        Requires.notNull(state,"state must not be null");
        this.stateRef.set(state);
    }

    @Override
    public final Session session() {
        return session;
    }
}
