import org.teamapps.message.protocol.message.AttributeType;
import org.teamapps.message.protocol.message.MessageDefinition;
import org.teamapps.message.protocol.message.MessageModelCollection;
import org.teamapps.message.protocol.model.ModelCollection;
import org.teamapps.message.protocol.model.ModelCollectionProvider;

public class MessageProtocol implements ModelCollectionProvider {

	@Override
	public ModelCollection getModelCollection() {
        MessageModelCollection model = new MessageModelCollection("{name}", "{namespace}", {version});
{model}

        return model;
    }
}