package dhxz.session.core;

import java.util.concurrent.atomic.AtomicReference;

public abstract class SessionSupport implements Session {
    private AtomicReference<Metadata> metadataRef = new AtomicReference<>();

    @Override
    public final Metadata metadata() {
        return this.metadataRef.get();
    }

    @Override
    public void metadata(Metadata metadata) {
        this.metadataRef.set(metadata);
    }
}
