package com.gcplot.commons;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;

public abstract class FileUtils {

    public static void deleteSilent(File f) {
        try {
            delete(f);
        } catch (IOException ignored) {
        }
    }

    public static void delete(File f) throws IOException {
        clearFolder(f);
        f.delete();
    }

    public static void clearFolder(File f) throws IOException {
        if (f.isDirectory()) {
            for (File c : f.listFiles())
                delete(c);
        }
    }

    public static File findFile(String folder, String start) throws IOException {
        File dir = new File(folder);
        File[] files = dir.listFiles((f,n) -> n.startsWith(start));
        if (files.length == 0) {
            return null;
        } else {
            return files[0];
        }
    }

    public static String getFileChecksum(MessageDigest digest, File file) throws IOException
    {
        try (BufferedInputStream fis = new BufferedInputStream(new FileInputStream(file))) {
            byte[] byteArray = new byte[8 * 1024];
            int bytesCount = 0;

            while ((bytesCount = fis.read(byteArray)) != -1) {
                digest.update(byteArray, 0, bytesCount);
            }
        }
        byte[] bytes = digest.digest();

        StringBuilder sb = new StringBuilder();
        for (byte aByte : bytes) {
            sb.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
        }

        //return complete hash
        return sb.toString();
    }

}