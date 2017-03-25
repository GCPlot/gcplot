package com.gcplot.messages;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.util.UUID;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         8/28/16
 */
public class AddJvmRequest {
    @JsonProperty(value = "id", required = true)
    @JsonDeserialize(using = UUIDDeserializer.class)
    @JsonSerialize(using = UUIDSerializer.class)
    public UUID jvmId;
    @JsonProperty(value = "an_id", required = true)
    public String analyseId;
    @JsonProperty(value = "name", required = true)
    public String jvmName;
    @JsonProperty(value = "vm_ver", required = true)
    public int vmVersion;
    @JsonProperty(value = "gc_type", required = true)
    public int gcType;
    @JsonProperty("headers")
    public String headers;
    @JsonProperty("mem")
    public MemoryStatus memoryStatus;

    public AddJvmRequest(@JsonProperty(value = "id", required = true)
                         @JsonDeserialize(using = UUIDDeserializer.class)
                         @JsonSerialize(using = UUIDSerializer.class) UUID jvmId,
                         @JsonProperty(value = "an_id", required = true) String analyseId,
                         @JsonProperty(value = "name", required = true) String jvmName,
                         @JsonProperty(value = "vm_ver", required = true) int vmVersion,
                         @JsonProperty(value = "gc_type", required = true) int gcType,
                         @JsonProperty("headers") String headers,
                         @JsonProperty("mem") MemoryStatus memoryStatus) {
        this.jvmId = jvmId;
        this.analyseId = analyseId;
        this.jvmName = jvmName;
        this.vmVersion = vmVersion;
        this.gcType = gcType;
        this.headers = headers;
        this.memoryStatus = memoryStatus;
    }

    public static class UUIDDeserializer extends StdDeserializer<UUID> {
        public UUIDDeserializer() {
            super(UUID.class);
        }

        @Override
        public UUID deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return UUID.fromString(p.getValueAsString());
        }
    }

    public static class UUIDSerializer extends StdSerializer<UUID> {
        public UUIDSerializer() {
            super(UUID.class);
        }

        @Override
        public void serialize(UUID value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeString(value.toString());
        }
    }
}
