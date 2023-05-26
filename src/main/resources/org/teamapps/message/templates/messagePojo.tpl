package {package};

import org.teamapps.message.protocol.message.*;
import org.teamapps.message.protocol.model.*;
import org.teamapps.message.protocol.service.*;
import org.teamapps.message.protocol.file.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import java.lang.invoke.MethodHandles;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.time.*;



public class {type} extends Message {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final static PojoObjectDecoder<{type}> decoder = new PojoObjectDecoder<{type}>() {
		@Override
		public {type} decode(DataInputStream dis, FileDataReader fileDataReader) {
			try {
				return new {type}(dis, fileDataReader);
			} catch (IOException e) {
				LOGGER.error("Error creating {type} instance", e);
			}
			return null;
		}

		@Override
		public {type} decode(Element element, FileDataReader fileDataReader) {
			return new {type}(element, fileDataReader);
		}

        @Override
        public {type} decode(String xml, FileDataReader fileDataReader) {
            try {
                return new {type}(xml, fileDataReader);
            } catch (Exception e) {
                LOGGER.error("Error creating {type} instance", e);
            }
            return null;
        }

		@Override
		public {type} defaultMessage() {
			return ({type}) new {type}().setDefaultValues({schema}.MODEL_COLLECTION);
		}

		@Override
		public {type} remap(MessageRecord message) {
			return new {type}(message, {schema}.MODEL_COLLECTION);
		}

        @Override
        public String getMessageUuid() {
            return OBJECT_UUID;
        }
	};

	public static PojoObjectDecoder<{type}> getMessageDecoder() {
		return decoder;
	}

	public static MessageModel getMessageModel() {
        return {schema}.MODEL_COLLECTION.getModel(OBJECT_UUID);
    }

	public static ModelCollection getModelCollection() {
		return {schema}.MODEL_COLLECTION;
	}

    public static {type} remap(MessageRecord message) {
        return new {type}(message, {schema}.MODEL_COLLECTION);
    }

    public final static String OBJECT_UUID = "{uuid}";


	public {type}() {
		super({schema}.MODEL_COLLECTION.getModel(OBJECT_UUID));
	}

	public {type}(MessageRecord message, ModelCollection modelCollection) {
		super(message, modelCollection);
	}

	public {type}(DataInputStream dis) throws IOException {
		super(dis, {schema}.MODEL_COLLECTION.getModel(OBJECT_UUID), null, {schema}.MODEL_COLLECTION);
	}

	public {type}(DataInputStream dis, FileDataReader fileDataReader) throws IOException {
		super(dis, {schema}.MODEL_COLLECTION.getModel(OBJECT_UUID), fileDataReader, {schema}.MODEL_COLLECTION);
	}

	public {type}(byte[] bytes) throws IOException {
		super(bytes, {schema}.MODEL_COLLECTION.getModel(OBJECT_UUID), null, {schema}.MODEL_COLLECTION);
	}

	public {type}(byte[] bytes, FileDataReader fileDataReader) throws IOException {
		super(bytes, {schema}.MODEL_COLLECTION.getModel(OBJECT_UUID), fileDataReader, {schema}.MODEL_COLLECTION);
	}

	public {type}(Element element, FileDataReader fileDataReader) {
		super(element, {schema}.MODEL_COLLECTION.getModel(OBJECT_UUID), fileDataReader, {schema}.MODEL_COLLECTION);
	}

    public {type}(String xml, FileDataReader fileDataReader) throws Exception {
        super(xml, {schema}.MODEL_COLLECTION.getModel(OBJECT_UUID), fileDataReader, {schema}.MODEL_COLLECTION);
    }

{methods}

}