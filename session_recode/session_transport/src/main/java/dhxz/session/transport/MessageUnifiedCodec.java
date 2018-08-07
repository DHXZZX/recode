package dhxz.session.transport;

import io.netty.channel.CombinedChannelDuplexHandler;

public class MessageUnifiedCodec extends CombinedChannelDuplexHandler<MessageUnifiedDecoder,MessageUnifiedEncoder> {
    public MessageUnifiedCodec(){
        super(new MessageUnifiedDecoder(),new MessageUnifiedEncoder());
    }
}
