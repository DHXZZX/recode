package dhxz.session.integrations;

import dhxz.session.core.SessionContext;
import dhxz.session.spi.MessageHandler;
import org.springframework.stereotype.Component;
import poggyio.dataschemas.Message;
import poggyio.dataschemas.ParticipantType;

@Component
public class MeetingHandler implements MessageHandler {
    @Override
    public boolean supports(Message message) {
        return message.toType() == ParticipantType.MEETING;
    }

    @Override
    public void handle(SessionContext context, Message message) {

    }
}
