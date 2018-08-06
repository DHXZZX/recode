package dhxz.session.integrations;

import dhxz.session.core.SessionContext;
import dhxz.session.spi.MessageHandler;
import org.dayatang.utils.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import poggyio.commons.logger.Loggers;
import poggyio.dataschemas.Message;
import poggyio.dataschemas.ParticipantType;
import poggyio.schd.api.TaskService;
import poggyio.schd.integrations.serveplatform.Client2AppTask;

@Component
public class AppHandler implements MessageHandler {
    private TaskService taskService;

    /**
     * @param taskService
     */
    @Autowired
    public AppHandler(TaskService taskService) {
        Assert.notNull(taskService, "taskService must not be null.");
        this.taskService = taskService;
    }

    @Override
    public boolean supports(Message message) {
        return message.toType() == ParticipantType.APP;
    }

    @Override
    public void handle(SessionContext context, Message message) {
        Loggers.me().info(getClass(), "ServeHandler messgae:{}", message);
        Client2AppTask task = Client2AppTask.newBuilder()
                .domainId(context.domainId())
                .orgId(orgId(message))
                .clientId(context.clientId())
                .deviceId(context.deviceId())
                .message(message)
                .build();
        taskService.submit(task);
    }

    private String orgId(Message message) {
        String orgId = message.body("@org_id");
        if (StringUtils.isEmpty(orgId)) {
            return message.body("org_id");
        }

        return orgId;
    }
}
