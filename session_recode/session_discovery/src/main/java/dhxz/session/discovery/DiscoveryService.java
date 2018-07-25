package dhxz.session.discovery;

import poggyio.component.Component;

import java.util.List;
import java.util.Optional;

public interface DiscoveryService extends Component {

    void register(TransportInstance instance);

    void unregister(TransportInstance instance);

    Optional<TransportInstance> lookup(String clientEndpoint);

    List<TransportInstance> lookupAll(String clientEndpoint);

}
