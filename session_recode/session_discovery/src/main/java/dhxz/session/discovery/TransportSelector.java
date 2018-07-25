package dhxz.session.discovery;

import java.util.List;

public interface TransportSelector {

    TransportInstance select(String clientEndpoint, List<TransportInstance> instances);
}
