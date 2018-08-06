package dhxz.session.discovery;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Accessors(fluent = true,chain = true)
public class TransportInstance implements Serializable {
    private static final long serialVersionUID = 8032244718542540778L;

    private String id;
    private String host;
    private String version;
    private String protocol;
    private String platforms;

    private int port;
    private int weight;
    private int connections;
    private int maxConnections;

    private boolean sslEnable;
    private boolean sslVerify;
}
