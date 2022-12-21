package {package};

import org.teamapps.protocol.message.*;
import org.teamapps.protocol.service.*;
import org.teamapps.protocol.file.*;
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
		switch (method) {
{cases}
		}
		return null;
	}

}