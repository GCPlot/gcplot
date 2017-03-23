package com.gcplot.commons;

import com.gcplot.commons.exceptions.Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Array;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Supplier;

public abstract class Utils {
    private static final String hostname;

    static {
        try {
            String hm = System.getenv("HOSTNAME");
            if (hm == null) {
                hm = System.getProperty("current.host.name");
            }
            if (hm == null) {
                hm = InetAddress.getLocalHost().getHostName();
            }
            hostname = hm;
        } catch (UnknownHostException e) {
            throw Exceptions.runtime(e);
        }
    }

    public static Port[] getFreePorts(int portNumber) {
        try {
            Port[] result = new Port[portNumber];
            List<ServerSocket> servers = new ArrayList<>(portNumber);
            try {
                for (int i = 0; i < portNumber; i++) {
                    ServerSocket tempServer = new ServerSocket(0);
                    File file = new File(System.getProperty("java.io.tmpdir"),
                            tempServer.getLocalPort() + ".lock");

                    try {
                        FileChannel channel = new RandomAccessFile(file, "rw").getChannel();
                        FileLock lock = channel.lock();
                        result[i] = new Port(tempServer.getLocalPort(), file, lock, channel);
                        servers.add(tempServer);
                    } catch (Throwable t) {
                        try {
                            tempServer.close();
                        } catch (IOException ignored) {
                        }
                        try {
                            file.delete();
                        } catch (Throwable ignored) {
                        }
                        result[i] = getFreePorts(1)[0];
                    }
                }
            } finally {
                for (ServerSocket server : servers) {
                    try {
                        server.close();
                    } catch (IOException e) {
                        // Continue closing servers.
                    }
                }
            }
            return result;
        } catch (Throwable t) {
            throw Exceptions.runtime(t);
        }
    }

    public static String getHostName() {
        return hostname;
    }

    public static String getRandomIdentifier() {
        try {
            // The first step is to get a filename generator
            char lowerBound = 'a';
            char upperBound = 'z';
            SecureRandom randomCharacterGenerator = new SecureRandom();
            // Then get some characters
            char[] identifierBuffer = new char[512];
            int index = identifierBuffer.length;
            final int numericLowerBound = (int) lowerBound;
            final int numericUpperBound = (int) upperBound;
            final int range = numericUpperBound - numericLowerBound;
            do {
                int getOne = randomCharacterGenerator.nextInt(range);
                int next = numericLowerBound + getOne;
                identifierBuffer[--index] = (char) next;
            }
            while (index > 0x00000000);
            return new String(identifierBuffer);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw Exceptions.runtime(e);
        }
    }

    public static boolean waitFor(Supplier<Boolean> condition, long timeoutNanos) {
        long start = System.nanoTime();
        boolean result = false;
        while (System.nanoTime() - start < timeoutNanos) {
            result = condition.get();
            if (result) break;

            LockSupport.parkNanos(1);
        }
        return result;
    }

    public static String esc(String username) {
        return username.replaceAll("[^a-zA-Z0-9.-]", "_");
    }

    public static <T> T[] concat(T[] a, T[] b) {
        int aLen = a.length;
        int bLen = b.length;

        @SuppressWarnings("unchecked")
        T[] c = (T[]) Array.newInstance(a.getClass().getComponentType(), aLen+bLen);
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);

        return c;
    }

    private static final Logger LOG = LoggerFactory.getLogger(Utils.class);

    public static class Port {
        public final int value;
        private final File file;
        private final FileLock lock;
        private final FileChannel channel;

        public Port(int port, File file, FileLock lock, FileChannel channel) {
            this.value = port;
            this.file = file;
            this.lock = lock;
            this.channel = channel;
        }

        public void unlock() {
            try {
                try {
                    lock.release();
                } catch (Throwable ignored) {
                }
                try {
                    channel.close();
                } catch (Throwable ignored) {
                }
            } catch (Throwable ignored) {
            } finally {
                file.delete();
            }
        }
    }

}
