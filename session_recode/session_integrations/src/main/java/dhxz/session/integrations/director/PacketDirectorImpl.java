package dhxz.session.integrations.director;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.hash.Hashing;
import dhxz.session.core.PacketContext;
import dhxz.session.core.PacketContextSupport;
import dhxz.session.core.Session;
import dhxz.session.spi.PacketDirector;
import dhxz.session.spi.event.SessionEvent;
import lombok.Builder;
import lombok.Data;
import org.dayatang.utils.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import poggyio.commons.Constants;
import poggyio.dataschemas.Message;
import poggyio.dataschemas.Packet;
import poggyio.logging.Loggers;
import poggyio.uc.api.DeviceGetRequest;
import poggyio.uc.api.DeviceRecord;
import poggyio.uc.api.DeviceService;

import java.util.Base64;
import java.util.Objects;

@Service
public class PacketDirectorImpl implements PacketDirector {

    private static ObjectMapper OBJECT_MAPPER;
    static {
        OBJECT_MAPPER = new ObjectMapper();
        OBJECT_MAPPER.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
        OBJECT_MAPPER.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        OBJECT_MAPPER.configure(JsonGenerator.Feature.IGNORE_UNKNOWN, true);
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        OBJECT_MAPPER.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
        OBJECT_MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    }

    private static final String  DEVICE_RECORD_NAME = DeviceRecord.class.getName();
    private static final String DEVICE_SERVICE_NAME = DeviceService.class.getName();
    private final DeviceService deviceService;

    @Autowired
    public PacketDirectorImpl(DeviceService deviceService) {
        Assert.notNull(deviceService, "deviceService must not be null.");
        this.deviceService = deviceService;
    }

    @Override
    public void action(PacketContext context) {
        context.attr(DEVICE_RECORD_NAME,deviceService);
        context.state(MyStates.HANDSHAKE_WAIT);
    }

    enum MyStates implements PacketContextSupport.State {
        HANDSHAKE_WAIT {
            @Override
            public void advance(PacketContext context, Packet packet) throws Exception {

                if (packet.type() != Packet.CONNECT_TYPE) {
                    Loggers.me().warn(getClass(), "unexpect type[{}] on init state, ignore it.", packet.type());
                    context.state(CONNECT_RST);
                    context.advance(packet);
                    return;
                }

                ClientHello hello = OBJECT_MAPPER.readValue(packet.body(), ClientHello.class);
                DeviceService deviceService = context.attr(DEVICE_SERVICE_NAME);
                DeviceGetRequest deviceRequest = DeviceGetRequest.newBuilder()
                        .domainId(hello.domainId)
                        .clientId(hello.clientId)
                        .deviceId(hello.deviceId)
                        .build();
                DeviceRecord deviceRecord = deviceService.get(deviceRequest);
                if (null == deviceRecord) {
                    Loggers.me().info(
                            getClass(),
                            "device[domainId={},clientId={},deviceId={}] not found, handshake fail.",
                            hello.domainId,
                            hello.clientId,
                            hello.deviceId
                    );
                    context.state(HANDSHAKE_FAIL);
                    context.advance(packet);
                    return;
                }

                String raw = deviceRecord.deviceSecret() + hello.timestamp + hello.nonce;
                byte[] md5 = Hashing.md5().newHasher()
                        .putString(raw, Constants.DEFAULT_CHARSET)
                        .hash()
                        .asBytes();

                String expect = Base64.getEncoder().encodeToString(md5);
                if (!Objects.equals(expect, hello.signature)) {
                    Loggers.me().info(
                            getClass(),
                            "device[domainId={},clientId={},deviceId={}] with unexpect signature={},expect={}, handshake fail.",
                            hello.domainId,
                            hello.clientId,
                            hello.deviceId,
                            hello.signature,
                            expect
                    );
                    context.state(HANDSHAKE_FAIL);
                    context.advance(packet);
                    return;
                }

                context.attr(DEVICE_RECORD_NAME, deviceRecord);
                context.state(HANDSHAKE_ACK);
                context.advance(packet);
            }
        },

        HANDSHAKE_FAIL {
            @Override
            public void advance(PacketContext context, Packet packet) throws Exception {
                context.state(CONNECT_RST);
                context.advance(packet);
            }
        },

        HANDSHAKE_ACK {
            @Override
            public void advance(PacketContext context, Packet packet) throws Exception {

                DeviceRecord deviceRecord = context.attr(DEVICE_RECORD_NAME);
                Session session = context.session();
                Session.Metadata metadata = session.metadata()
                        .toBuilder()
                        .domainId(deviceRecord.domainId())
                        .clientId(deviceRecord.clientId())
                        .clientSecret(deviceRecord.deviceSecret())
                        .deviceId(deviceRecord.deviceId())
                        .devicePlatform(deviceRecord.systemPlatform())
                        .compressType(deviceRecord.compressType())
                        .digestType(deviceRecord.digestType())
                        .encryptType(deviceRecord.encryptType())
                        .state(Session.State.APPROVED)
                        .build();

                session.metadata(metadata);
                context.triggerEvent(SessionEvent.of(SessionEvent.Type.APPROVED, session));

                ServerHello hello = ServerHello.newBuilder()
                        .lastDeliveryId(session.lastHandledMessage().map(Message::deliveryId).orElse(""))
                        .lastDeliveryTime(session.lastHandledMessage().map(Message::deliveryTime).orElse(-1L))
                        .build();
                context.reply(Packet.empty().type(Packet.CONNECT_ACK_TYPE).body(OBJECT_MAPPER.writeValueAsBytes(hello)));
                context.state(CONNECTED);
            }
        },

        CONNECT_RST {
            @Override
            public void advance(PacketContext context, Packet packet) throws Exception {
                context.close(Packet.empty().type(Packet.CONNECT_RST_TYPE));
                context.state(HANDSHAKE_WAIT);
            }
        },

        CONNECTED {
            @Override
            public void advance(PacketContext context, Packet packet) throws Exception {

                if (packet.type() == Packet.POST_TYPE) {
                    context.through(packet);
                    return;
                }

                if (packet.type() == Packet.PING_TYPE) {
                    context.state(PING);
                    context.advance(packet);
                    return;
                }

                Loggers.me().warn(getClass(), "unexpect type[{}] on connected state, ignore it.", packet.type());
            }
        },

        PING {
            @Override
            public void advance(PacketContext context, Packet packet) throws Exception {
                Session session = context.session();
                ServerHello hello = ServerHello.newBuilder()
                        .lastDeliveryId(session.lastHandledMessage().map(Message::deliveryId).orElse(""))
                        .lastDeliveryTime(session.lastHandledMessage().map(Message::deliveryTime).orElse(-1L))
                        .build();
                context.reply(Packet.empty().type(Packet.PONG_TYPE).body(OBJECT_MAPPER.writeValueAsBytes(hello)));
                context.state(CONNECTED);
            }
        }
    }

    private static class ClientHello {
        String domainId;
        String clientId;
        String deviceId;
        String signature;
        long nonce;
        long timestamp;
    }

    @Data
    @Builder(builderMethodName = "newBuilder")
    private static class ServerHello {
        String timezone;
        long timestamp;
        String lastDeliveryId;
        long lastDeliveryTime;
    }
}
