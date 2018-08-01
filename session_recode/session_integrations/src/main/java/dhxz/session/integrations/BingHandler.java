package dhxz.session.integrations;

import dhxz.session.core.SessionContext;
import dhxz.session.spi.MessageHandler;
import poggyio.dataschemas.Message;
import poggyio.dataschemas.ParticipantType;
import poggyio.schd.api.TaskService;
import poggyio.schd.integrations.BingTask;

public class BingHandler implements MessageHandler {
    private TaskService taskService;

    public BingHandler(TaskService taskService) {
        this.taskService = taskService;
    }

    @Override
    public boolean supports(Message message) {
        return message.toType() == ParticipantType.BING;
    }

    @Override
    public void handle(SessionContext context, Message message) {
        BingTask task = BingTask.newBuilder()
                .domainId(context.domainId())
                .clientId(context.clientId())
                .deviceId(context.deviceId())
                .message(message)
                .build();

        taskService.submit(task);
    }
}
