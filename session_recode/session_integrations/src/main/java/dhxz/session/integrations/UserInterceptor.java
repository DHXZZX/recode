package dhxz.session.integrations;

import dhxz.session.core.SessionContext;
import dhxz.session.spi.MessageInterceptor;
import org.springframework.stereotype.Component;
import poggyio.dataschemas.Message;
import poggyio.logging.Loggers;

import static poggyio.lang.Strings.equalsIgnoreCase;

@Component
public class UserInterceptor implements MessageInterceptor {

    @Override
    public boolean beforeHandle(SessionContext context, Message message) {
        return equalsIgnoreCase(context.clientId(),message.from());
    }

    @Override
    public void afterHandle(SessionContext context, Message message, Exception error) {
        if (!equalsIgnoreCase(context.clientId(),message.from())) {
            Loggers.me().warn(
                    getClass(), "session[clientId={}] can't send message[{}], discard it.",
                    context.clientId(),
                    message
            );
        }
    }
}
