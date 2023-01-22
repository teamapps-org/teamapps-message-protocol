import org.teamapps.message.protocol.message.MessageDefinition;
import org.teamapps.message.protocol.message.MessageModelCollection;
import org.teamapps.message.protocol.model.ModelCollection;
import org.teamapps.message.protocol.model.ModelCollectionProvider;

public class MessageProtocol2 implements ModelCollectionProvider {
	@Override
	public ModelCollection getModelCollection() {
		MessageModelCollection modelCollection = new MessageModelCollection("newTestModel2", "org.teamapps.protocol.test", 1);


		MessageDefinition person2 = modelCollection.createModel("person2", "col.person", true);


		person2.addStringAttribute("name", 1);
		person2.addStringAttribute("phone", 4);


		return modelCollection;
	}
}
