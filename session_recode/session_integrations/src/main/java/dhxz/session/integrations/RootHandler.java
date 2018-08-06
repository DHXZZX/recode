package dhxz.session.integrations;

import dhxz.session.core.SessionContext;
import dhxz.session.spi.MessageHandler;
import org.springframework.stereotype.Component;
import poggyio.dataschemas.Message;

@Component
public class RootHandler implements MessageHandler {
    @Override
    public boolean supports(Message message) {
        return false;
    }

    @Override
    public void handle(SessionContext context, Message message) {

    }
}
