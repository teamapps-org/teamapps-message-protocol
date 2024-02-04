package org.teamapps.message.protocol.message;

import org.junit.Test;
import org.teamapps.protocol.test.NewTestModel;

import static org.junit.Assert.*;

public class ModelTest {

	@Test
	public void testModelCode() {
		String modelCode = NewTestModel.MODEL_COLLECTION.createModelCode();
		assertNotNull(modelCode);
		assertTrue(modelCode.contains("new MessageModelCollection(\"newTestModel\", \"org.teamapps.protocol.test\", 1);"));
	}
}
