package com.obj.nc.flows.dataSources.firestore.serialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.LongNode;

import java.io.IOException;
import java.time.Instant;

public class TimestampToInstantDeserializer extends StdDeserializer<Instant> {

    public TimestampToInstantDeserializer() {
        this(null);
    }

    public TimestampToInstantDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public Instant deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        TreeNode treeNode = p.getCodec().readTree(p);
        if (treeNode == null) {
            return null;
        }
        long seconds = ((LongNode) treeNode.get("seconds")).longValue();
        int nanos = ((IntNode) treeNode.get("nanos")).intValue();

        return Instant.ofEpochSecond(seconds, nanos);
    }
}
