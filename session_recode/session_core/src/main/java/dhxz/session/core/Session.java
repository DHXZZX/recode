package dhxz.session.core;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;
import poggyio.dataschemas.IData;
import poggyio.dataschemas.Message;

import java.util.Optional;

/**
 * @author siuming
 */

public interface Session {
    enum State {
        UNAPPROVED, APPROVED
        }

    /**
     * @return
     */
    Session.Metadata metadata();

    @Data
    @Builder(toBuilder = true)
    @Accessors(chain = true,fluent = true)
    class Metadata {
        private String domainId;
        private String clientId;
        private String clientSecret;
        private String deviceId;
        private String devicePlatform;

        private int compressType;
        private int digestType;
        private int encryptType;

        private String remoteEndpoint;
        private String accessEndpoint;
        private Session.State state;
    }
    /**
     *
     * @return
     */
    Optional<Message> lastHandledMessage();
    /**
     * @param metadata
     */
    void metadata(Session.Metadata metadata);
    /**
     * @param data
     */
    void reply(IData data);

    /**
     * @param data
     */
    void close(IData data);

}
