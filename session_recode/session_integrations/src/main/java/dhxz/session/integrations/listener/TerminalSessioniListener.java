package dhxz.session.integrations.listener;

import dhxz.session.core.Session;
import dhxz.session.spi.event.SessionEvent;
import dhxz.session.spi.event.SessionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import poggyio.lang.Requires;
import poggyio.lang.Strings;
import poggyio.logging.Loggers;
import poggyio.terminal.api.TerminalCloseRequest;
import poggyio.terminal.api.TerminalOpenRequest;
import poggyio.terminal.api.TerminalService;

@Service
public class TerminalSessioniListener implements SessionListener {

    private TerminalService terminalService;

    @Autowired
    public TerminalSessioniListener(TerminalService terminalService) {
        Requires.notNull(terminalService, "terminalService must not be null.");
        this.terminalService = terminalService;
    }


    @Override
    public void onEvent(SessionEvent event) {
        switch (event.type()) {
            case APPROVED:
                onSessionApproved(event.session());
                break;
            case CLOSED:
                onSessionClosed(event.session());
                break;
            default:
                break;
        }
    }

    private void onSessionApproved(Session session) {
        TerminalOpenRequest request = TerminalOpenRequest.newBuilder()
                .domainId(session.metadata().domainId())
                .clientId(session.metadata().clientId())
                .deviceId(session.metadata().deviceId())
                .devicePlatform(session.metadata().devicePlatform())
                .remoteEndpoint(session.metadata().remoteEndpoint())
                .accessEndpoint(session.metadata().accessEndpoint())
                .build();
        terminalService.terminalOpened(request);
    }

    private void onSessionClosed(Session session) {
        Loggers.me().info(getClass(), "session[{}] close.", session);
        if (session.metadata().state() == Session.State.UNAPPROVED) {
            return;
        }
        if (Strings.isBlank(session.metadata().domainId())
                || Strings.isBlank(session.metadata().clientId())) {
            return;
        }
        TerminalCloseRequest request = TerminalCloseRequest.newBuilder()
                .domainId(session.metadata().domainId())
                .clientId(session.metadata().clientId())
                .remoteEndpoint(session.metadata().remoteEndpoint())
                .build();
        terminalService.terminalClosed(request);
    }

}
