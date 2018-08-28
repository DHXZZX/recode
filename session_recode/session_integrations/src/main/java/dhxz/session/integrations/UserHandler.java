package dhxz.session.integrations;

import dhxz.session.core.SessionContext;
import dhxz.session.spi.MessageHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import poggyio.dataschemas.Message;
import poggyio.dataschemas.ParticipantType;
import poggyio.schd.api.TaskService;
import poggyio.schd.integrations.UserTask;

@Component
public class UserHandler implements MessageHandler {
    private final TaskService taskService;

    @Autowired
    public UserHandler(TaskService taskService) {
        this.taskService = taskService;
    }

    @Override
    public boolean supports(Message message) {
        return message.toType() == ParticipantType.USER;
    }

    @Override
    public void handle(SessionContext context, Message message) {
        UserTask task = UserTask.newBuilder()
                .domainId(context.domainId())
                .clientId(context.clientId())
                .deviceId(context.deviceId())
                .message(message)
                .build();
        taskService.submit(task);
    }
}
