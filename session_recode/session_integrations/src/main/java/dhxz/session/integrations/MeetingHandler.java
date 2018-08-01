package dhxz.session.integrations;

import dhxz.session.core.SessionContext;
import dhxz.session.spi.MessageHandler;
import poggyio.dataschemas.Message;
import poggyio.dataschemas.ParticipantType;

public class MeetingHandler implements MessageHandler {
    @Override
    public boolean supports(Message message) {
        return message.toType() == ParticipantType.MEETING;
    }

    @Override
    public void handle(SessionContext context, Message message) {

    }
}
