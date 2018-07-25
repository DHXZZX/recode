package dhxz.session.core;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;
import poggyio.lang.Requires;

import java.io.Serializable;

@Data
@Builder(toBuilder = true)
@Accessors(fluent = true,chain = true)
public class SessionContext implements Serializable {
    private static final long serialVersionUID = 5147974178627182529L;

    private String domainId;
    private String clientId;
    private String deviceId;
    private String devicePlatform;
    private String accessEndpoint;
    private String remoteEndpoint;
    private Session.State state;

    public boolean isApproved() {
        return state() == Session.State.APPROVED;
    }

    public static SessionContext from(Session session) {
        Requires.notNull(session,"session must not be null.");
        return builder()
                .domainId(session.metadata().domainId())
                .clientId(session.metadata().clientId())
                .deviceId(session.metadata().deviceId())
                .devicePlatform(session.metadata().devicePlatform())
                .accessEndpoint(session.metadata().accessEndpoint())
                .remoteEndpoint(session.metadata().remoteEndpoint())
                .state(session.metadata().state())
                .build();
    }

}
