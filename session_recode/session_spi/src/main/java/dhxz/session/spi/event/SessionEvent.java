package dhxz.session.spi.event;

import dhxz.session.core.Session;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import poggyio.dataschemas.Event;
import poggyio.lang.Requires;

@Data
@Accessors(fluent = true,chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class SessionEvent extends Event {
    private static final long serialVersionUID = -6527414988618503612L;

    public enum Type {
        OPENED,APPROVED,CLOSED
    }
    private Type type;
    private Session session;

    public static SessionEvent of(Type type,Session session) {
        Requires.notNull(type, "type must not be null.");
        Requires.notNull(session, "session must not be null.");
        return new SessionEvent(type,session);
    }
}
