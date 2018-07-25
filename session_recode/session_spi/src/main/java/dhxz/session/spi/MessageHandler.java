package dhxz.session.spi;

import dhxz.session.core.SessionContext;
import poggyio.dataschemas.Message;

public interface MessageHandler {

    boolean supports(Message message);

    void handle(SessionContext context,Message message);
}
