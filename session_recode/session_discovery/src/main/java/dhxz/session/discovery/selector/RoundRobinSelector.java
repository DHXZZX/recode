package dhxz.session.discovery.selector;

import dhxz.session.discovery.TransportInstance;
import dhxz.session.discovery.TransportSelector;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class RoundRobinSelector implements TransportSelector {

    private AtomicLong counter = new AtomicLong(0);

    @Override
    public TransportInstance select(String clientEndpoint, List<TransportInstance> instances) {
        if (instances.isEmpty()) {
            return null;
        }
        int index = (int)(counter.incrementAndGet() % instances.size());
        return instances.get(index);
    }
}
