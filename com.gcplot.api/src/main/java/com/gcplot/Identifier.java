package com.gcplot;

import com.gcplot.commons.BinaryUtils;
import com.gcplot.commons.exceptions.Exceptions;

import java.io.UnsupportedEncodingException;
import java.util.zip.CRC32;

public interface Identifier {

    byte[] bin();

    long num();

    default Identifier fromStr(String id) {
        return new StringBasedIdentifier(id);
    }

    default Identifier fromLong(long id) {
        return new LongBasedIdentifier(id);
    }

    class StringBasedIdentifier implements Identifier {

        @Override
        public byte[] bin() {
            return bin;
        }

        @Override
        public long num() {
            crc.get().update(bin);
            return crc.get().getValue();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            StringBasedIdentifier that = (StringBasedIdentifier) o;

            return id.equals(that.id);

        }

        @Override
        public int hashCode() {
            return id.hashCode();
        }

        @Override
        public String toString() {
            return id;
        }

        public StringBasedIdentifier(String id) {
            this.id = id;
            try {
                this.bin = id.getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw Exceptions.runtime(e);
            }
        }

        private final String id;
        private final byte[] bin;
        private static final ThreadLocal<CRC32> crc = new ThreadLocal<CRC32>() {
            @Override
            protected CRC32 initialValue() {
                return new CRC32();
            }
        };
    }

    class LongBasedIdentifier implements Identifier {

        @Override
        public byte[] bin() {
            byte[] b = new byte[8];
            BinaryUtils.longToBytes(id, b);
            return b;
        }

        @Override
        public long num() {
            return id;
        }

        public LongBasedIdentifier(long id) {
            this.id = id;
        }

        private final long id;
    }

}
