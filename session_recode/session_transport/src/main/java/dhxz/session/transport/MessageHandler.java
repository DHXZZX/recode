package dhxz.session.transport;

import dhxz.session.core.Session;
import dhxz.session.core.SessionContext;
import dhxz.session.spi.MessageHandlerNotFoundException;
import dhxz.session.spi.MessageInterceptor;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import poggyio.dataschemas.Message;
import poggyio.dataschemas.PacketException;
import poggyio.dataschemas.ParticipantType;
import poggyio.dataschemas.body.CmdBody;
import poggyio.logging.Loggers;

import java.util.List;
import java.util.Optional;

public final class MessageHandler extends ChannelDuplexHandler {
    private static final String SYSTEM = "system";
    private static final String RECONNECT = "reconnect";
    private final List<dhxz.session.spi.MessageHandler> messageHandlers;
    private final List<MessageInterceptor> messageInterceptors;

    public MessageHandler(List<dhxz.session.spi.MessageHandler> messageHandlers, List<MessageInterceptor> messageInterceptors) {
        this.messageHandlers = messageHandlers;
        this.messageInterceptors = messageInterceptors;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof Message) {
            messageReceived(ctx, (Message) msg);
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    private void messageReceived(ChannelHandlerContext ctx, Message msg) {
        Session session = SessionHolder.getSession(ctx);
        Optional<dhxz.session.spi.MessageHandler> handlerOptional = getHandler(msg);
        if (!handlerOptional.isPresent()) {
            throw new MessageHandlerNotFoundException();
        }
        SessionContext context = SessionContext.from(session);
        handlerOptional.get().handle(context,msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause instanceof DecoderException
                || cause instanceof EncoderException
                || cause instanceof PacketException
                || cause instanceof MessageHandlerNotFoundException) {
            Loggers.me().warn(getClass(), "packet or message codec exception or handler not found exception, for prevent dirty data, close session.", cause);

            Session session = SessionHolder.getSession(ctx);
            Message reason = Message.newBuilder()
                    .fromType(ParticipantType.SYSTEM)
                    .fromDomain(session.metadata().domainId())
                    .from(SYSTEM)
                    .toType(ParticipantType.USER)
                    .toDomain(session.metadata().domainId())
                    .to(session.metadata().clientId())
                    .bodyType(Message.BodyType.CMD)
                    .body(CmdBody.COMMAND_TYPE_KEY, CmdBody.RECONNECT_COMMAND_REASON)
                    .body(CmdBody.COMMAND_REASON_KEY, CmdBody.RECONNECT_COMMAND_REASON)
                    .build();
            ctx.writeAndFlush(reason).addListener(ChannelFutureListener.CLOSE);
            return;
        }
        // unknown error, close session.
        Loggers.me().error(getClass(), "handle message occurs error. close session.", cause);
        ctx.close();
    }

    private Optional<dhxz.session.spi.MessageHandler> getHandler(Message msg) {
        return messageHandlers.stream()
                .filter(handler -> handler.supports(msg))
                .map(handler ->(dhxz.session.spi.MessageHandler)new InterceptorMessageHandler(handler,messageInterceptors))
                .findFirst();
    }

    static final class InterceptorMessageHandler implements dhxz.session.spi.MessageHandler {
        private final dhxz.session.spi.MessageHandler messageHandler;
        private final List<MessageInterceptor> messageInterceptors;

        public InterceptorMessageHandler(dhxz.session.spi.MessageHandler messageHandler, List<MessageInterceptor> messageInterceptors) {
            this.messageHandler = messageHandler;
            this.messageInterceptors = messageInterceptors;
        }

        @Override
        public boolean supports(Message message) {
            return this.messageHandler.supports(message);
        }

        @Override
        public void handle(SessionContext context, Message message) {
            int index = messageInterceptors.size() - 1;
            Exception error = null;
            try {
                for (int i = 0; i < messageInterceptors.size(); i++) {
                    index = i;
                    if (!messageInterceptors.get(i).beforeHandle(context, message)) {
                        return;
                    }
                }
                this.messageHandler.handle(context, message);
            } catch (Exception e) {
                error = e;
            } finally {
                for (int i = 0; i >= 0; i--) {
                    messageInterceptors.get(i).afterHandle(context, message, error);
                }
            }
        }
    }
}
