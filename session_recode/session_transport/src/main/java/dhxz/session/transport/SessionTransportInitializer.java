package dhxz.session.transport;

import dhxz.session.discovery.DiscoveryService;
import dhxz.session.discovery.TransportInstance;
import org.dayatang.utils.Assert;
import poggyio.component.ComponentInitializer;
import poggyio.component.TypedComponentListener;
import poggyio.lang.UUIDs;

public class SessionTransportInitializer extends ComponentInitializer<SessionTransport,SessionTransportInitializer.Listener> {


    public SessionTransportInitializer(SessionTransport transport, DiscoveryService discoveryService) {
        super(transport, new Listener(discoveryService));
    }

    static class Listener extends TypedComponentListener<SessionTransport> {
        private final String transportId = UUIDs.uuid();
        private final DiscoveryService discoveryService;

        public Listener(DiscoveryService discoveryService) {

            Assert.notNull(discoveryService, "discoveryService must not be null.");
            this.discoveryService = discoveryService;
        }

        @Override
        protected void afterComponentStarted(SessionTransport component) {
            discoveryService.register(getTransportInstance(component));
        }

        private TransportInstance getTransportInstance(SessionTransport transport) {
            return TransportInstance
                    .builder()
                    .host(transport.sessionConfig().paramOrDefault("canonical.hostname",transport.sessionConfig().host()))
                    .port(transport.sessionConfig().intParamOrDefault("canonical.port",transport.sessionConfig().port()))
                    .protocol(transport.sessionConfig().scheme())
                    .id(transport.sessionConfig().paramOrDefault("id",transportId))
                    .weight(transport.sessionConfig().intParamOrDefault("weight",-1))
                    .version(transport.sessionConfig().param("version"))
                    .platforms(transport.sessionConfig().param("platforms"))
                    .sslEnable(transport.sessionConfig().booleanParamOrDefault("ssl.enabled",false))
                    .sslVerify(transport.sessionConfig().booleanParamOrDefault("ssl.verify",false))
                    .build();
        }
    }

}
