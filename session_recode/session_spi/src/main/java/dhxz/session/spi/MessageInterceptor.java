package dhxz.session.spi;

import dhxz.session.core.SessionContext;
import poggyio.dataschemas.Message;

public interface MessageInterceptor {

    default boolean beforeHandle(SessionContext context, Message message) {
        return false;
    }

    default void afterHandle(SessionContext context, Message message,Exception error){}
}
