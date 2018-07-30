package dhxz.session.dubbo;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.cluster.LoadBalance;
import com.google.common.base.Joiner;
import dhxz.session.api.SessionCloseRequest;
import dhxz.session.api.SessionNotFoundException;
import dhxz.session.api.SessionPostRequest;
import poggyio.net.QueryStringDecoder;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SessionServiceLoadBalance implements LoadBalance {

    private static final String POST_METHOD = "post";
    private static final String CLOSE_METHOD = "close";

    @Override
    public <T> Invoker<T> select(List<Invoker<T>> invokers, URL url, Invocation invocation) throws RpcException {
        return null;
    }

    private String getAccessEndpoint(Invocation invocation) {
        Object req = invocation.getArguments()[0];
        switch (invocation.getMethodName()) {
            case POST_METHOD:
                return ((SessionPostRequest) req).accessEndpoint();
            case CLOSE_METHOD:
                return ((SessionCloseRequest) req).accessEndpoint();
            default:
                return ((SessionCloseRequest) req).accessEndpoint();
        }
    }

    private <T> Invoker<T> doSelect(List<Invoker<T>> invokers, String accessEndpoint) {
        Map<String, List<String>> params = new QueryStringDecoder(accessEndpoint).parameters();
        String serviceHost = Joiner.on("").join(params.getOrDefault("service.host", Collections.EMPTY_LIST));
        String servicePort = Joiner.on("").join(params.getOrDefault("service.port", Collections.EMPTY_LIST));

        return invokers.stream()
                .filter(it -> matchesAddress(it, serviceHost, servicePort))
                .findAny()
                .orElseThrow(SessionNotFoundException::new);
    }

    private <T> boolean matchesAddress(Invoker<T> invoker, String serviceHost, String servicePort) {
        int port = invoker.getUrl().getPort();
        String host = invoker.getUrl().getHost();
        return Objects.equals(host, serviceHost) && Objects.equals(port, servicePort);
    }
}
