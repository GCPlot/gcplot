package com.gcplot.repository;

import com.gcplot.cassandra.BaseCassandra;
import com.gcplot.cassandra.CassandraConnector;
import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.junit.Before;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class BaseCassandraTest extends BaseCassandra {
    protected CassandraConnector connector;

    @Before
    public void before() throws Exception {
        super.before();
        connector = new CassandraConnector();
        connector.setHosts(new String[]{EmbeddedCassandraServerHelper.getHost()});
        connector.setPort(nativePort);
        connector.setKeyspace(null);
        connector.init();
        String q = readFile(new File(getClass().getClassLoader().getResource("cassandra-scheme.sql").toURI()).getAbsolutePath(), Charsets.UTF_8);
        for (String qq : q.split(";")) {
            if (!Strings.isNullOrEmpty(qq.trim())) {
                connector.session().execute(qq.trim());
            }
        }
        connector.destroy();
        connector.setKeyspace("gcplot");
        connector.init();
    }

    @SuppressWarnings("all")
    public static <K, T> Map<K, T> map(Object... objs) {
        if (objs.length % 2 != 0) {
            throw new IllegalArgumentException("Input map should contain even count of arguments.");
        }

        Map<K, T> map = new HashMap<>();

        for (int i = 0; i < objs.length; i += 2) {
            map.put((K) objs[i], (T) objs[i + 1]);
        }

        return map;
    }

    private static String readFile(String path, Charset encoding)
            throws IOException
    {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }
}
