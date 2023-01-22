package {package};

import org.teamapps.message.protocol.message.*;
import org.teamapps.message.protocol.service.*;
import org.teamapps.message.protocol.file.*;

import java.io.IOException;

public class {type} extends AbstractClusterServiceClient {

    public {type}(ClusterServiceRegistry registry) {
        super(registry, "{serviceName}");
    }

{methods}

}