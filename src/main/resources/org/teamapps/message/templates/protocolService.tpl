package {package};

import org.teamapps.message.protocol.message.*;
import org.teamapps.message.protocol.service.*;
import org.teamapps.message.protocol.file.*;
import java.io.IOException;

public abstract class {type} extends AbstractClusterService {

    public {type}() {
        super("{serviceName}");
    }

    public {type}(ClusterServiceRegistry registry) {
        super(registry, "{serviceName}");
    }

{methods}
	@Override
	public Message handleMessage(String method, Message request) {
		return switch (method) {
{cases}
			default -> null;
		};
	}

	@Override
	public void handleBroadcastMessage(String method, Message request) {
		switch (method) {
{broadcastCases}
		}
	}
}