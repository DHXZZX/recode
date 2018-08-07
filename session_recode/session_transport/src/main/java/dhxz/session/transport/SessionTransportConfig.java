package dhxz.session.transport;

import poggyio.lang.Requires;
import poggyio.lang.Strings;
import poggyio.net.QueryStringDecoder;

import java.net.URI;
import java.util.List;

public final class SessionTransportConfig {

    private final URI accessEndpoint;

    public SessionTransportConfig(String accessEndpoint) {
        Requires.hasText(accessEndpoint, "accessEndpoint must not be null or empty.");
        this.accessEndpoint = URI.create(accessEndpoint).normalize();
    }

    public String scheme() {
        return accessEndpoint.getScheme();
    }

    public String host() {
        return accessEndpoint.getHost();
    }
    public int port() {
        return accessEndpoint.getPort();
    }
    public String path() {
        return accessEndpoint.getPath();
    }
    public URI accessEndpoint() {
        return accessEndpoint;
    }
    public String param(String name) {
        return paramOrDefault(name,"");
    }

    public String paramOrDefault(String name, String defaultValue) {
        List<String> params = new QueryStringDecoder(accessEndpoint).parameters().get(name);
        return null == params || params.isEmpty() ? defaultValue : params.get(0);
    }

    public int intParam(String name) {
        return intParamOrDefault(name,-1);
    }

    public int intParamOrDefault(String name, int defaultValue) {
        String value = param(name);
        if (Strings.isBlank(value)) {
            return defaultValue;
        }

        return Integer.parseInt(value);
    }

    public long longParam(String name) {
        return longParamOrDefault(name, -1L);
    }

    public long longParamOrDefault(String name, long defaultValue) {
        String value = param(name);
        if (Strings.isBlank(value)) {
            return defaultValue;
        }

        return Long.parseLong(value);
    }

    public float floatParam(String name) {
        return floatParamOrDefault(name, -1f);
    }

    public float floatParamOrDefault(String name, float defaultValue) {
        String value = param(name);
        if (Strings.isBlank(value)) {
            return defaultValue;
        }

        return Float.parseFloat(value);
    }

    public boolean booleanParam(String name) {
        return booleanParamOrDefault(name, false);
    }

    public boolean booleanParamOrDefault(String name, boolean defaultValue) {
        String value = param(name);
        if (Strings.isBlank(value)) {
            return defaultValue;
        }

        return Boolean.valueOf(value);
    }
}
