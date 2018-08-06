package dhxz.session.integrations;

import dhxz.session.core.SessionContext;
import dhxz.session.spi.MessageHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import poggyio.dataschemas.Message;
import poggyio.dataschemas.ParticipantType;
import poggyio.schd.api.TaskService;
import poggyio.schd.integrations.DiscussionTask;

@Slf4j
@Component
public class DiscussionHandler implements MessageHandler {
    private TaskService taskService;

    @Autowired
    public DiscussionHandler(TaskService taskService) {
        this.taskService = taskService;
    }

    @Override
    public boolean supports(Message message) {
        return message.toType() == ParticipantType.DISCUSSION;
    }

    @Override
    public void handle(SessionContext context, Message message) {
        log.info("handle discussion message:{}",message.toString());
        DiscussionTask task = DiscussionTask.newBuilder()
                .domainId(context.domainId())
                .clientId(context.clientId())
                .deviceId(context.deviceId())
                .message(message)
                .build();

        taskService.submit(task);
    }
}
