package com.gcplot.commons;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.security.MessageDigest;
import java.util.zip.GZIPOutputStream;

public abstract class FileUtils {
    private static final Logger LOG = LoggerFactory.getLogger(FileUtils.class);

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

    public static String getFileChecksum(MessageDigest digest, InputStream fis) throws IOException {
        byte[] byteArray = new byte[8 * 1024];
        int bytesCount = 0;

        while ((bytesCount = fis.read(byteArray)) != -1) {
            digest.update(byteArray, 0, bytesCount);
        }
        byte[] bytes = digest.digest();

        StringBuilder sb = new StringBuilder();
        for (byte aByte : bytes) {
            sb.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
        }

        //return complete hash
        return sb.toString();
    }

    public static File gzip(File file) {
        byte[] buffer = new byte[8 * 1024];
        File result = null;
        try {
            result = new File(file.getParentFile(), file.getName() + ".gz");
            if (!result.exists()) {
                result.createNewFile();
            }
            try (FileInputStream fos = new FileInputStream(file)) {
                try (GZIPOutputStream gzos = new GZIPOutputStream(new FileOutputStream(result))) {
                    int len;
                    while ((len = fos.read(buffer)) > 0) {
                        gzos.write(buffer, 0, len);
                    }
                    gzos.finish();
                }
            }
            return result;
        } catch (Throwable t) {
            LOG.error(t.getMessage(), t);
            if (result != null) {
                FileUtils.deleteSilent(result);
            }
            return file;
        }
    }

}