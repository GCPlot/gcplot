package com.gcplot.commons;

import com.gcplot.commons.exceptions.Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

public abstract class Utils {

    public static int[] getFreePorts(int portNumber) {
        try {
            int[] result = new int[portNumber];
            List<ServerSocket> servers = new ArrayList<>(portNumber);
            try {
                for (int i = 0; i < portNumber; i++) {
                    ServerSocket tempServer = new ServerSocket(0);
                    servers.add(tempServer);
                    result[i] = tempServer.getLocalPort();
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
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            LOG.error(e.getMessage(), e);
            return "unknown_host";
        }
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

    private static final Logger LOG = LoggerFactory.getLogger(Utils.class);

}
