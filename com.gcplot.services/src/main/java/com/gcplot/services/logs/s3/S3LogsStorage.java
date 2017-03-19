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

import static com.gcplot.commons.Utils.esc;

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
                String key = s.getKey().replace(prefix, "");
                if (key.startsWith("-") || key.startsWith("/")) {
                    key = key.substring(key.indexOf('/') + 1);
                }
                String[] parts = key.split("/");
                if (parts.length == 4) {
                    return new LogHandle(parts[3], parts[0], parts[1], parts[2]);
                } else {
                    return LogHandle.INVALID_LOG;
                }
            }

            private boolean loadIfRequired() {
                if (result != null && (result.isTruncated() || result.getContinuationToken() == null)) {
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

    @Override
    public LogSource get(LogHandle handle) {
        return new S3LogSource(handle, client().getObject(connector.getBucket(), handlePath(handle)));
    }

    @Override
    public void delete(LogHandle handle) {
        client().deleteObject(connector.getBucket(), handlePath(handle));
    }

    protected String handlePath(LogHandle handle) {
        return prefix +
                esc(handle.getUsername()) + "/" + esc(handle.getAnalyzeId()) + "/" +
                esc(handle.getJvmId()) + "/" + esc(handle.getName());
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
