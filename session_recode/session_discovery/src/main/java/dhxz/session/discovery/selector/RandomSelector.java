package dhxz.session.discovery.selector;

import dhxz.session.discovery.TransportInstance;
import dhxz.session.discovery.TransportSelector;

import java.util.List;
import java.util.Random;

public class RandomSelector implements TransportSelector{

    private final Random random = new Random();

    @Override
    public TransportInstance select(String clientEndpoint, List<TransportInstance> instances) {
        if (instances.isEmpty()) {
            return null;
        }
        int index = random.nextInt(instances.size());

        return instances.get(index);
    }
}
