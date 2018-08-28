package dhxz.session.transport;

import dhxz.session.core.Session;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;

public final class SessionHolder {
    static final AttributeKey<Session> SESSION_KEY = AttributeKey.valueOf(Session.class.getName());

    public static Session getSession(ChannelHandlerContext ctx) {
        return ctx.channel().attr(SESSION_KEY).get();
    }

    public static Session getAndRemoveSession(ChannelHandlerContext ctx){
        return ctx.channel().attr(SESSION_KEY).getAndSet(null);
    }

    public static void setSession(ChannelHandlerContext ctx,Session session){
        ctx.channel().attr(SESSION_KEY).set(session);
    }

    private SessionHolder(){}
}
