package com.gcplot.commons.serialization;

import com.gcplot.commons.exceptions.Exceptions;
import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

import java.io.ByteArrayOutputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         3/13/17
 */
@SuppressWarnings("all")
public abstract class ProtostuffSerializer {
    private static final ThreadLocal<LinkedBuffer> BUFFER = ThreadLocal.withInitial(LinkedBuffer::allocate);
    private static final ThreadLocal<ByteArrayOutputStream> OUT = ThreadLocal.withInitial(ByteArrayOutputStream::new);
    private static final Map<Class, Schema> schemas = new ConcurrentHashMap<>();

    public static byte[] serialize(Object message) {
        Schema schema = schemas.computeIfAbsent(message.getClass(), RuntimeSchema::createFrom);
        ByteArrayOutputStream baos = OUT.get();
        try {
            ProtostuffIOUtil.writeTo(baos, message, schema, BUFFER.get());
            return baos.toByteArray();
        } catch (Throwable t) {
            throw Exceptions.runtime(t);
        } finally {
            BUFFER.get().clear();
            baos.reset();
        }
    }

    public static <T> T deserialize(Class<T> type, byte[] payload) {
        Schema schema = schemas.computeIfAbsent(type, RuntimeSchema::createFrom);
        Object msg = schema.newMessage();
        ProtostuffIOUtil.mergeFrom(payload, msg, schema);
        return (T) msg;
    }

}
