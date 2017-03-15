package com.gcplot.services.logs.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.gcplot.fs.LogsStorage;
import com.gcplot.logs.LogHandle;
import com.gcplot.logs.LogSource;
import com.gcplot.services.S3Connector;

import java.util.Iterator;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         3/15/17
 */
public class S3LogsStorage implements LogsStorage {
    private S3Connector connector;
    private String prefix;

    @Override
    public Iterator<LogHandle> listAll() {
        final ListObjectsV2Request req = new ListObjectsV2Request()
                .withBucketName(connector.getBucket()).withPrefix(prefix);
        return new Iterator<LogHandle>() {
            ListObjectsV2Result result;
            Iterator<S3ObjectSummary> i;

            @Override
            public boolean hasNext() {
                if (i == null || !i.hasNext()) {
                    return loadIfRequired();
                } else {
                    return i.hasNext();
                }
            }

            @Override
            public LogHandle next() {
                S3ObjectSummary s = i.next();
                return null;
            }

            private boolean loadIfRequired() {
                if (result != null && result.isTruncated()) {
                    return false;
                } else {
                    result = client().listObjectsV2(req);
                    req.setContinuationToken(result.getNextContinuationToken());
                    i = result.getObjectSummaries().iterator();
                    return i.hasNext();
                }
            }
        };
    }

    public static void main(String[] args) {
        S3Connector c = new S3Connector();
        c.setAccessKey("AKIAICRUH6IDD2ZUFAOA");
        c.setSecretKey("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");
        c.setBucket("gcplot");
        c.init();

        S3LogsStorage s = new S3LogsStorage();
        s.setConnector(c);
        s.setPrefix("gc-log");

        Iterator<LogHandle> i = s.listAll();
        while (i.hasNext()) {
            i.next();
        }
    }

    @Override
    public LogSource get(LogHandle handle) {
        return null;
    }

    @Override
    public void delete(LogHandle handle) {

    }

    private AmazonS3 client() {
        return connector.getClient();
    }

    public void setConnector(S3Connector connector) {
        this.connector = connector;
    }

    public void setPrefix(String prefix) {
        if (!prefix.endsWith("/")) {
            prefix += '/';
        }
        this.prefix = prefix;
    }
}
