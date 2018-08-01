package dhxz.session.integrations;

import dhxz.session.core.SessionContext;
import dhxz.session.spi.MessageHandler;
import poggyio.dataschemas.Message;
import poggyio.dataschemas.body.ACKBody;
import poggyio.lang.Requires;
import poggyio.lang.Strings;
import poggyio.logging.Loggers;
import poggyio.schd.api.TaskService;
import poggyio.schd.integrations.ReadAckTask;
import poggyio.schd.integrations.RecvAckTask;
import poggyio.schd.integrations.RemoveAckTask;

import java.util.List;

public class ACKHandler implements MessageHandler {

    private final TaskService taskService;

    public ACKHandler(TaskService taskService) {
        Requires.notNull(taskService, "taskService must not be null.");
        this.taskService = taskService;
    }

    @Override
    public boolean supports(Message message) {
        return Message.BodyType.ACK == message.bodyType();
    }

    @Override
    public void handle(SessionContext context, Message message) {
        String ackType = message.body(ACKBody.ACK_TYPE_KEY);
        List<String> ackIds = message.body(ACKBody.ACK_IDS_KEY);
        if (Strings.isBlank(ackType)) {
            Loggers.me().warn(getClass(), "ack type not present, ignore it.");
            return;
        }
        boolean ackForward = parseACKForward(message);

        if (Strings.equalsIgnoreCase(ackType,ACKBody.RECV_ACK_TYPE)) {
            RecvAckTask task = RecvAckTask.newBuilder()
                    .domainId(context.domainId())
                    .clientId(context.clientId())
                    .deviceId(context.deviceId())
                    .ackForward(ackForward)
                    .ackIds(ackIds)
                    .message(message)
                    .build();

            taskService.submit(task);
            return;
        }

        if (Strings.equalsIgnoreCase(ackType,ACKBody.READ_ACK_TYPE)) {
            ReadAckTask task = ReadAckTask.newBuilder()
                    .domainId(context.domainId())
                    .clientId(context.clientId())
                    .deviceId(context.deviceId())
                    .ackForward(ackForward)
                    .ackIds(ackIds)
                    .message(message)
                    .build();
            taskService.submit(task);
            return;
        }
        if (Strings.equalsIgnoreCase(ackType,ACKBody.REMOVE_ACK_TYPE)) {
            RemoveAckTask task = RemoveAckTask.newBuilder()
                    .domainId(context.domainId())
                    .clientId(context.clientId())
                    .deviceId(context.deviceId())
                    .ackIds(ackIds)
                    .ackForward(ackForward)
                    .message(message)
                    .build();
        }

        Loggers.me().warn(getClass(), "unknown ack type [" + ackType + "], ignore it.");
    }

    private boolean parseACKForward(Message message) {
        try {
            int ackForward = message.body(ACKBody.ACK_FORWARD_KEY);
            return ackForward ==1;
        }catch (Exception ignore) {
            Loggers.me().info(getClass(), "parse ack forward occurs error.", ignore);
            return false;
        }
    }
}
