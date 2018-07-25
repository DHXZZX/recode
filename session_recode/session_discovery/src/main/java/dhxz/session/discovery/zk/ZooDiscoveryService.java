package dhxz.session.discovery.zk;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import dhxz.session.discovery.DiscoveryException;
import dhxz.session.discovery.DiscoveryService;
import dhxz.session.discovery.TransportInstance;
import dhxz.session.discovery.TransportSelector;
import dhxz.session.discovery.selector.RoundRobinSelector;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.apache.curator.x.discovery.*;
import org.apache.curator.x.discovery.details.InstanceSerializer;
import poggyio.component.AbstractComponent;
import poggyio.lang.Requires;
import poggyio.lang.Strings;
import poggyio.logging.Loggers;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

public class ZooDiscoveryService extends AbstractComponent implements DiscoveryService {

    private static final String SERVICE_NAME = TransportInstance.class.getSimpleName();

    private CuratorFramework serviceClient;
    private ServiceDiscovery<TransportInstance> serviceDiscovery;

    private final String namespace;
    private final String zkServices;
    private TransportSelector transportSelector = new RoundRobinSelector();

    public ZooDiscoveryService(String namespace, String zkServices) {
        Requires.hasText(namespace, "namespace must not be null or empty.");
        Requires.hasText(zkServices, "zkservers must not be null or empty.");
        this.namespace = namespace;
        this.zkServices = zkServices;
    }

    @Override
    public void register(TransportInstance instance) {
        try {
            unregister(instance);
            ServiceInstance<TransportInstance> serviceInstance = ServiceInstance.<TransportInstance>builder()
                    .serviceType(ServiceType.DYNAMIC)
                    .id(instance.id())
                    .name(SERVICE_NAME)
                    .address(instance.host())
                    .port(instance.port())
                    .payload(instance)
                    .build();
            serviceDiscovery.registerService(serviceInstance);
        } catch (Exception e) {
            throw new DiscoveryException("register transport error.",e);
        }
    }

    @Override
    public void unregister(TransportInstance instance) {
        Requires.notNull(instance,"instance must not be null.");
        try {
            ServiceInstance<TransportInstance> service = serviceDiscovery.queryForInstance(SERVICE_NAME, instance.id());
            if (Objects.nonNull(service)) {
                serviceDiscovery.unregisterService(service);
            }
        } catch (Exception e) {
            throw new DiscoveryException("unregister transport error.",e);
        }
    }

    @Override
    public Optional<TransportInstance> lookup(String clientEndpoint) {
        List<TransportInstance> instances = lookupAll(clientEndpoint);
        return Optional.ofNullable(transportSelector.select(clientEndpoint,instances));
    }

    @Override
    public List<TransportInstance> lookupAll(String clientEndpoint) {
        Requires.hasText(clientEndpoint,"clientEndpoint must not be null or empty");
        ServiceProvider<TransportInstance> provider = null;
        try {
            provider = providerAcquire(platformSatisfiedBy(clientEndpoint));
            return provider.getAllInstances().stream().map(ServiceInstance::getPayload).collect(toList());
        } catch (Exception e) {
            throw new DiscoveryException("lookup transport error.",e);
        }finally {
            providerRelease(provider);
        }
    }

    private void providerRelease(ServiceProvider<TransportInstance> provider) {
        if (provider == null) {
            return;
        }
        try {
            provider.close();
        } catch (IOException e) {
            Loggers.me().warn(getClass(), "close service provider error.", e);
        }
    }

    private InstanceFilter<TransportInstance> platformSatisfiedBy(String clientEndpoint) {
        return it -> {
            assert it != null;
            String platforms = Strings.upcase(it.getPayload().platforms());
            String clientPlatform = Strings.upcase(URI.create(clientEndpoint).getScheme());
            return Strings.contains(platforms,clientPlatform);
        };
    }

    private ServiceProvider<TransportInstance> providerAcquire(InstanceFilter<TransportInstance> filter) throws Exception {
        ServiceProvider<TransportInstance> provider = serviceDiscovery.serviceProviderBuilder()
                .serviceName(SERVICE_NAME)
                .additionalFilter(filter)
                .build();

        provider.start();
        return provider;
    }

    @Override
    protected void doStart() {
        try {
            startServiceClient();
            startServiceDiscovery();
        } catch (Exception e) {
            throw new DiscoveryException("discover start error.",e);
        }
    }

    private void startServiceClient() {
        serviceClient = CuratorFrameworkFactory.builder()
                .namespace(namespace)
                .connectString(zkServices)
                .retryPolicy(new RetryNTimes(Integer.MAX_VALUE,1000))
                .connectionTimeoutMs(5000)
                .build();
        serviceClient.start();
    }

    private void startServiceDiscovery() throws Exception {
        serviceDiscovery = ServiceDiscoveryBuilder.builder(TransportInstance.class)
                .basePath("")
                .client(serviceClient)
                .serializer(new TransportInstanceSerializer())
                .build();
        serviceDiscovery.start();
    }

    private static class TransportInstanceSerializer implements InstanceSerializer<TransportInstance> {

        final ObjectMapper objectMapper;

        public TransportInstanceSerializer() {
            objectMapper = new ObjectMapper();
            objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
            objectMapper.setVisibility(PropertyAccessor.FIELD,JsonAutoDetect.Visibility.ANY);
        }

        @Override
        public byte[] serialize(ServiceInstance<TransportInstance> instance) throws Exception {
            return objectMapper.writeValueAsBytes(instance);
        }

        @Override
        public ServiceInstance<TransportInstance> deserialize(byte[] bytes) throws Exception {
            JavaType javaType = objectMapper.getTypeFactory().constructParametrizedType(
                    ServiceInstance.class,ServiceInstance.class,TransportInstance.class
            );
            return objectMapper.readValue(bytes,javaType);
        }
    }

    @Override
    protected void doStop() {
        stopServiceCLient();
        stopServiceDiscovery();
    }

    private void stopServiceCLient() {
        if (null == serviceClient) {
            return;
        }
        try {
            serviceClient.close();
        }catch (Exception e) {
            Loggers.me().warn(getClass(), "close service client error. ", e);
        }
    }

    private void stopServiceDiscovery() {
        if (null == serviceDiscovery) {
            return;
        }
        try {
            serviceDiscovery.close();
        } catch (Exception e) {
            Loggers.me().warn(getClass(), "close service discovery error. ", e);
        }
    }


    public void setTransportSelector(TransportSelector transportSelector) {
        Requires.notNull(transportSelector, "selector must not be null.");
        this.transportSelector = transportSelector;
    }
}
