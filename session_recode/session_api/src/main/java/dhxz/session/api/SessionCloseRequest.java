package dhxz.session.api;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;
import poggyio.dataschemas.IData;

import java.io.Serializable;

@Data
@Builder(toBuilder = true)
@Accessors(fluent = true,chain = true)
public class SessionCloseRequest implements Serializable {
    private static final long serialVersionUID = 7067611394981017137L;

    private String accessEndpoint;
    private String remoteEndpoint;
    private IData data;
}
